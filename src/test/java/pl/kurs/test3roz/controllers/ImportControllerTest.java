package pl.kurs.test3roz.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.test3roz.exceptions.ImportException;
import pl.kurs.test3roz.exceptions.ImportParseException;
import pl.kurs.test3roz.imports.ImportExecutor;
import pl.kurs.test3roz.imports.ImportLock;
import pl.kurs.test3roz.imports.ImportProperties;
import pl.kurs.test3roz.imports.ImportQueue;
import pl.kurs.test3roz.imports.ImportService;
import pl.kurs.test3roz.imports.ImportTask;
import pl.kurs.test3roz.imports.ImportTaskRepository;
import pl.kurs.test3roz.imports.ImportWorker;
import pl.kurs.test3roz.imports.PersonFileImporter;
import pl.kurs.test3roz.imports.csv.CsvLineToCommandParser;
import pl.kurs.test3roz.imports.models.Import;
import pl.kurs.test3roz.imports.models.ImportStatus;
import pl.kurs.test3roz.imports.models.ImportStatusDto;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
@Transactional
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImportService importService;

    @Autowired
    private ImportLock importLock;

    @Autowired
    private ImportProperties importProperties;

    @Autowired
    private ApplicationContext context;

    @BeforeEach
    void setUp() {
        importLock.unlock();
    }

    @Test
    void shouldNotUseLockWhenParallelImportsAllowed() throws Exception {
        importProperties.setAllowParallelImports(true);

        String content = """
                type,firstName,lastName,pesel,height,weight,email,gender,password,hireDate,endDate,currentPosition,currentSalary
                EMPLOYEE,Anna,Nowak,90010112345,165.0,60.0,test_email1@example.com,FEMALE,haslo123,2022-01-01,2030-01-01,Developer,5000
                """;

        MockMultipartFile file = new MockMultipartFile("file", "parallel.csv", "text/csv", content.getBytes());

        String response = mockMvc.perform(multipart("/api/imports").file(file))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        String importId = extractImportId(response);
        Thread.sleep(500);

        mockMvc.perform(get("/api/imports/status/" + importId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.failed").value(false));
    }

    @Test
    void shouldProcessCsvStreamAndCountProcessedRows() throws Exception {
        String csv = """
        type,firstName,lastName,pesel,height,weight,email,gender,password,hireDate,endDate,currentPosition,currentSalary
        EMPLOYEE,Anna,Nowak,90010112345,165.0,60.0,test_email1@example.com,FEMALE,haslo123,2022-01-01,2030-01-01,Developer,5000
        EMPLOYEE,Jan,Kowalski,85050512345,180.0,80.0,test_email2@example.com,MALE,haslo456,2019-03-01,2025-03-01,Manager,8000
        """;

        AtomicLong processedRows = new AtomicLong(0);
        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        PersonFileImporter importer = context.getBean(PersonFileImporter.class);

        importer.run(inputStream, processedRows);

        assertThat(processedRows.get()).isEqualTo(2);
    }

    @Test
    void shouldSaveEntityAndUnlockLock() throws Exception {
        ImportTask task = new ImportTask();
        task.setImportId("test-id");
        task.setFilePath("src/test/resources/test.csv");

        Import entity = new Import();
        entity.setImportId("test-id");
        entity.setStatus(ImportStatus.PROCESSING);
        entity.setFilePath(task.getFilePath());

        ImportQueue queue = Mockito.mock(ImportQueue.class);
        ImportExecutor executor = Mockito.mock(ImportExecutor.class);
        ImportLock lock = Mockito.mock(ImportLock.class);
        ImportService importService = Mockito.mock(ImportService.class);
        ImportTaskRepository taskRepository = Mockito.mock(ImportTaskRepository.class);

        Mockito.when(queue.take()).thenReturn(task).thenReturn(null);
        Mockito.when(importService.getStatus("test-id")).thenReturn(entity);
        Mockito.when(lock.tryLock()).thenReturn(true);
        Mockito.doAnswer(invocation -> null).when(executor).run(Mockito.any(), Mockito.any(), Mockito.any());

        ImportWorker worker = new ImportWorker(queue, executor, lock, importService, taskRepository);

        Thread thread = new Thread(() -> {
            try {
                worker.importWorkerRunner().run(null);
            } catch (Exception ignored) {
            }
        });
        thread.start();
        Thread.sleep(500);

        Mockito.verify(taskRepository, Mockito.atLeastOnce()).save(entity);
        Mockito.verify(lock, Mockito.atLeastOnce()).unlock();

        thread.interrupt();
        thread.join();
    }

    @Test
    void shouldThrowWhenImportIdNotFound() {
        String unknownId = "non-existent-id";

        assertThatThrownBy(() -> importService.getStatusDto(unknownId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Import with id " + unknownId + " not found");
    }

    @Test
    void shouldReturnCorrectImportStatusDto() {
        ImportTaskRepository repo = context.getBean(ImportTaskRepository.class);
        ImportService service = context.getBean(ImportService.class);

        Import entity = new Import();
        entity.setImportId("test-id-status");
        entity.setStatus(ImportStatus.PROCESSING);
        entity.setCreatedAt(LocalDateTime.now().minusHours(1));
        entity.setFinishedAt(LocalDateTime.now());
        entity.setProcessedRows(5L);
        repo.save(entity);

        ImportStatusDto dto = service.getStatusDto("test-id-status");

        assertThat(dto.getImportId()).isEqualTo("test-id-status");
        assertThat(dto.getStatus()).isEqualTo(ImportStatus.PROCESSING.name());
        assertThat(dto.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(dto.getFinishedAt()).isEqualTo(entity.getFinishedAt());
        assertThat(dto.getProcessedRows()).isEqualTo(5L);
        assertThat(dto.isFailed()).isFalse();
        assertThat(dto.isRunning()).isTrue();
    }

    @Test
    void shouldThrowImportParseExceptionWhenInputStreamIsInvalid() {
        PersonFileImporter importer = context.getBean(PersonFileImporter.class);

        InputStream badInput = InputStream.nullInputStream();

        AtomicLong processedRows = new AtomicLong(0);

        assertThatThrownBy(() -> importer.run(badInput, processedRows))
                .isInstanceOf(ImportParseException.class)
                .hasMessage("Import failed");
    }


    @Test
    void shouldRunImportExecutor(){
        ImportExecutor importExecutor = context.getBean(ImportExecutor.class);

        String csv = """
        type,firstName,lastName,pesel,height,weight,email,gender,password,hireDate,endDate,currentPosition,currentSalary
        EMPLOYEE,Anna,Nowak,90010112345,165.0,60.0,test_email@example.com,FEMALE,haslo123,2022-01-01,2030-01-01,Developer,5000
        """;

        AtomicLong processedRows = new AtomicLong(0);
        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        importExecutor.run(inputStream, processedRows, context.getBean(ImportLock.class));

        assertThat(processedRows.get()).isGreaterThan(0);
    }


    @Test
    void shouldThrowRuntimeExceptionWhenFileTransferFails() throws Exception {
        ImportService importService = context.getBean(ImportService.class);

        MultipartFile badFile = Mockito.mock(MultipartFile.class);
        Mockito.doThrow(new RuntimeException("fail"))
                .when(badFile)
                .transferTo(Mockito.any(File.class));

        assertThatThrownBy(() -> importService.importCsv(badFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Import failed")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldSubmitAllPendingAndProcessingTasksToQueue() {
        ImportService importService = context.getBean(ImportService.class);
        ImportQueue mockQueue = Mockito.mock(ImportQueue.class);
        ImportTaskRepository importTaskRepository = context.getBean(ImportTaskRepository.class);

        Import import1 = new Import();
        import1.setImportId("id1");
        import1.setFilePath("/tmp/file1.csv");
        import1.setStatus(ImportStatus.PENDING);

        Import import2 = new Import();
        import2.setImportId("id2");
        import2.setFilePath("/tmp/file2.csv");
        import2.setStatus(ImportStatus.PROCESSING);

        importTaskRepository.save(import1);
        importTaskRepository.save(import2);
        importService.resumePendingTasks(mockQueue);

        Mockito.verify(mockQueue).submit(Mockito.argThat(task ->
                task.getImportId().equals("id1") && task.getFilePath().equals("/tmp/file1.csv")
        ));
        Mockito.verify(mockQueue).submit(Mockito.argThat(task ->
                task.getImportId().equals("id2") && task.getFilePath().equals("/tmp/file2.csv")
        ));
    }

    @Test
    void shouldReturnImportEntityWhenExists() {
        ImportService importService = context.getBean(ImportService.class);
        ImportTaskRepository repo = context.getBean(ImportTaskRepository.class);

        String id = "existing-id";
        Import entity = new Import();
        entity.setImportId(id);
        repo.save(entity);

        Import result = importService.getStatus(id);

        assertThat(result).isNotNull();
        assertThat(result.getImportId()).isEqualTo(id);
    }

    @Test
    void shouldReturnFalseFromIsLockedWhenParallelImportsAllowed() {
        importProperties.setAllowParallelImports(true);
        boolean locked = importLock.isLocked();
        assertThat(locked).isFalse();
    }

    @Test
    void tryLockShouldReturnTrueWhenParallelImportsAllowed() {
        importProperties.setAllowParallelImports(true);
        boolean result = importLock.tryLock();
        assertThat(result).isTrue();
    }

    @Test
    void unlockShouldNotCallUnlockWhenNotHeldByCurrentThread() {
        importProperties.setAllowParallelImports(false);

        RLock mockLock = Mockito.mock(RLock.class);
        Mockito.when(mockLock.isHeldByCurrentThread()).thenReturn(false);

        RedissonClient mockClient = Mockito.mock(RedissonClient.class);
        Mockito.when(mockClient.getLock(Mockito.anyString())).thenReturn(mockLock);

        ImportLock lock = new ImportLock(mockClient, importProperties);

        lock.unlock();
        Mockito.verify(mockLock, Mockito.never()).unlock();
    }

    @Test
    void isLockedShouldReturnTrueWhenParallelImportsNotAllowed() {
        importProperties.setAllowParallelImports(false);

        RLock mockLock = Mockito.mock(RLock.class);
        Mockito.when(mockLock.isLocked()).thenReturn(true);

        RedissonClient mockClient = Mockito.mock(RedissonClient.class);
        Mockito.when(mockClient.getLock(Mockito.anyString())).thenReturn(mockLock);

        ImportLock lock = new ImportLock(mockClient, importProperties);

        boolean result = lock.isLocked();

        org.assertj.core.api.Assertions.assertThat(result).isTrue();
        Mockito.verify(mockClient).getLock("import_lock");
        Mockito.verify(mockLock).isLocked();
    }

    @Test
    void shouldHandleInterruptedExceptionWhileTakingFromQueue() throws Exception {
        ImportQueue mockQueue = Mockito.mock(ImportQueue.class);
        ImportExecutor mockExecutor = Mockito.mock(ImportExecutor.class);
        ImportLock mockLock = Mockito.mock(ImportLock.class);
        ImportService mockImportService = Mockito.mock(ImportService.class);
        ImportTaskRepository mockTaskRepository = Mockito.mock(ImportTaskRepository.class);

        Mockito.when(mockQueue.take()).thenThrow(new InterruptedException());

        ImportWorker worker = new ImportWorker(mockQueue, mockExecutor, mockLock, mockImportService, mockTaskRepository);

        Runnable runnable = () -> {
            try {
                worker.importWorkerRunner().run(null);
            } catch (Exception ignored) {
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
        Thread.sleep(100);
        thread.interrupt();
    }

    @Test
    void shouldRescheduleTaskWhenLockUnavailable() throws Exception {
        ImportTask task = Mockito.mock(ImportTask.class);
        Mockito.when(task.getImportId()).thenReturn("123");
        Mockito.when(task.getFilePath()).thenReturn("/tmp/file.csv");

        ImportQueue queue = Mockito.mock(ImportQueue.class);
        ImportExecutor executor = Mockito.mock(ImportExecutor.class);
        ImportLock lock = Mockito.mock(ImportLock.class);
        ImportService importService = Mockito.mock(ImportService.class);
        ImportTaskRepository taskRepository = Mockito.mock(ImportTaskRepository.class);

        Import importEntity = new Import();
        importEntity.setImportId("123");
        importEntity.setFilePath("/tmp/file.csv");
        importEntity.setStatus(ImportStatus.PENDING);
        Mockito.when(importService.getStatus("123")).thenReturn(importEntity);

        Mockito.when(queue.take()).thenReturn(task).thenAnswer(i -> {
            Thread.sleep(1000);
            return null;
        });

        Mockito.when(lock.tryLock()).thenReturn(false);

        ImportWorker worker = new ImportWorker(queue, executor, lock, importService, taskRepository);

        new Thread(() -> {
            try {
                worker.importWorkerRunner().run(null);
            } catch (Exception ignored) {
            }
        }).start();

        Thread.sleep(300);
        Mockito.verify(queue).submit(task);
    }

    @Test
    void shouldLogUnexpectedErrorWhenExceptionThrown() throws Exception {
        ImportTask task = Mockito.mock(ImportTask.class);
        Mockito.when(task.getImportId()).thenReturn("123");
        Mockito.when(task.getFilePath()).thenReturn("/tmp/file.csv");

        ImportQueue queue = Mockito.mock(ImportQueue.class);
        ImportExecutor executor = Mockito.mock(ImportExecutor.class);
        ImportLock lock = Mockito.mock(ImportLock.class);
        ImportService importService = Mockito.mock(ImportService.class);
        ImportTaskRepository taskRepository = Mockito.mock(ImportTaskRepository.class);

        Import importEntity = new Import();
        importEntity.setImportId("123");
        importEntity.setFilePath("/tmp/file.csv");
        importEntity.setStatus(ImportStatus.PENDING);
        Mockito.when(importService.getStatus("123")).thenReturn(importEntity);

        Mockito.when(queue.take()).thenReturn(task).thenReturn(null);
        Mockito.when(lock.tryLock()).thenThrow(new RuntimeException("Simulated"));

        ImportWorker worker = new ImportWorker(queue, executor, lock, importService, taskRepository);
        worker.importWorkerRunner().run(null);
    }

    @Test
    void shouldThrowForUnknownType() {
        CsvLineToCommandParser parser = context.getBean(CsvLineToCommandParser.class);
        String[] header = {"type", "firstName"};
        String[] values = {"FAKE", "John"};

        assertThatThrownBy(() -> parser.parse(values, header))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown type");
    }

    @Test
    void shouldCreateImportExceptionWithMessage() {
        assertThatThrownBy(() -> {
            throw new ImportException("Something went wrong");
        }).isInstanceOf(ImportException.class)
                .hasMessage("Something went wrong");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStatusNotFound() {
        String unknownId = "fakeid";

        assertThatThrownBy(() -> importService.getStatus(unknownId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Import with id " + unknownId + " not found");
    }

    private String extractImportId(String response) throws Exception {
        return new ObjectMapper()
                .readTree(response)
                .get("importId")
                .asText();
    }
}
