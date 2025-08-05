package pl.kurs.test3roz.controllers;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.test3roz.exceptions.ImportException;
import pl.kurs.test3roz.imports.ImportExecutor;
import pl.kurs.test3roz.imports.ImportLock;
import pl.kurs.test3roz.imports.ImportProperties;
import pl.kurs.test3roz.imports.ImportQueue;
import pl.kurs.test3roz.imports.ImportService;
import pl.kurs.test3roz.imports.ImportStatus;
import pl.kurs.test3roz.imports.ImportTask;
import pl.kurs.test3roz.imports.ImportWorker;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @BeforeEach
    void setUp() {
        importLock.unlock();
    }


    @Test
    void shouldReturnStatusWithoutStartingImport() throws Exception {
        mockMvc.perform(get("/api/imports/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldStartCsvImport() throws Exception {
        byte[] content = Files.readAllBytes(Paths.get("src/test/resources/test-import.csv"));
        MockMultipartFile file = new MockMultipartFile("file", "test-import.csv", "text/csv", content);

        String response = mockMvc.perform(multipart("/api/imports").file(file))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        String importId = extractImportId(response);

        await().atMost(2, SECONDS).untilAsserted(() ->
                mockMvc.perform(get("/api/imports/status/" + importId).with(user("test").roles("IMPORTER")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.running").value(false))
                        .andExpect(jsonPath("$.processedRows").value(3))
        );
    }

    @Test
    void shouldSetFailedWhenTooFewCommonFields() throws Exception {
        String content = """
        type,firstName
        EMPLOYEE,John
        """;

        MockMultipartFile file = new MockMultipartFile("file", "bad.csv", "text/csv", content.getBytes());

        String response = mockMvc.perform(multipart("/api/imports").file(file))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        String importId = extractImportId(response);

        Thread.sleep(500);

        mockMvc.perform(get("/api/imports/status/" + importId))
                .andExpect(jsonPath("$.failed").value(true));
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

        Mockito.when(mockQueue.take()).thenThrow(new InterruptedException());

        ImportWorker worker = new ImportWorker(mockQueue, mockExecutor, mockLock);

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
        ImportStatus status = new ImportStatus();
        status.setId("123");
        Mockito.when(task.getStatus()).thenReturn(status);

        ImportQueue queue = Mockito.mock(ImportQueue.class);
        ImportExecutor executor = Mockito.mock(ImportExecutor.class);
        ImportLock lock = Mockito.mock(ImportLock.class);

        Mockito.when(queue.take()).thenReturn(task).thenAnswer(i -> { Thread.sleep(1000); return null; });
        Mockito.when(lock.tryLock()).thenReturn(false);

        new Thread(() -> {
            try {
                new ImportWorker(queue, executor, lock).importWorkerRunner().run(null);
            } catch (Exception ignored) {}
        }).start();

        Thread.sleep(300);
        Mockito.verify(queue).submit(task);
    }

    @Test
    void shouldLogUnexpectedErrorWhenExceptionThrown() throws Exception {
        ImportTask task = Mockito.mock(ImportTask.class);
        ImportStatus status = new ImportStatus();
        status.setId("123");
        Mockito.when(task.getStatus()).thenReturn(status);

        ImportQueue queue = Mockito.mock(ImportQueue.class);
        ImportExecutor executor = Mockito.mock(ImportExecutor.class);
        ImportLock lock = Mockito.mock(ImportLock.class);

        Mockito.when(queue.take()).thenReturn(task).thenReturn(null);
        Mockito.when(lock.tryLock()).thenThrow(new RuntimeException("Simulated"));

        new ImportWorker(queue, executor, lock).importWorkerRunner().run(null);
    }


    @Test
    void shouldStartImportButFailAndSetStatusAccordingly() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv",
                "type,firstName,lastName,pesel,height,weight,email,gender\nEMPLOYEE,A,B,INVALID_PESEL,180,75,a@b.pl,MALE".getBytes()
        );

        String response = mockMvc.perform(multipart("/api/imports").file(file))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        String importId = extractImportId(response);

        Thread.sleep(500);

        mockMvc.perform(get("/api/imports/status/" + importId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.failed").value(true))
                .andExpect(jsonPath("$.running").value(false));
    }

    @Test
    void shouldSetFailedStatusAndUnlockWhenImportThrowsException() throws Exception {
        importProperties.setAllowParallelImports(true);
        importLock.unlock();

        MockMultipartFile brokenFile = new MockMultipartFile(
                "file",
                "broken.csv",
                "text/csv",
                "type,firstName,lastName,pesel,height,weight,email,gender,password\nEMPLOYEE,A,B,INVALID_PESEL,180,75,a@b.pl,MALE,pass".getBytes()
        );

        String response = mockMvc.perform(multipart("/api/imports").file(brokenFile))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        String importId = extractImportId(response);

        Thread.sleep(500);

        mockMvc.perform(get("/api/imports/status/" + importId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.failed").value(true))
                .andExpect(jsonPath("$.running").value(false));

        assertThat(importLock.isLocked()).isFalse();
    }


    @Test
    void shouldCreateImportExceptionWithMessage() {
        assertThatThrownBy(() -> {
            throw new ImportException("Something went wrong");
        }).isInstanceOf(ImportException.class)
                .hasMessage("Something went wrong");
    }

    @Test
    void shouldFailImportWhenUnknownTypeProvided() throws Exception {
        String content = """
            type,firstName,lastName,pesel,height,weight,email,salary
            xxxxx,John,Doe,12345678901,180,75,john@example.com,10000
            """;

        MockMultipartFile file = new MockMultipartFile("file", "people.csv", "text/csv", content.getBytes());

        String response = mockMvc.perform(multipart("/api/imports").file(file)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        String importId = extractImportId(response);

        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            mockMvc.perform(get("/api/imports/status/" + importId)
                            .with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.failed").value(true))
                    .andExpect(jsonPath("$.running").value(false));
        });
    }

    @Test
    void shouldFailImportWhenJsonProcessingExceptionOccurs() throws Exception {
        String content = """
            type,firstName,lastName,pesel,height,weight,email,salary
            employee,John,Doe,12345678901,180,75,john@example.com,xxxxx
            """;

        MockMultipartFile file = new MockMultipartFile("file", "people.csv", "text/csv", content.getBytes());

        String response = mockMvc.perform(multipart("/api/imports").file(file)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        String importId = extractImportId(response);

        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            mockMvc.perform(get("/api/imports/status/" + importId)
                            .with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.failed").value(true))
                    .andExpect(jsonPath("$.running").value(false));
        });
    }
    @Test
    void shouldCreateImportTaskAndAccessFields() {
        InputStream mockStream = new ByteArrayInputStream("data".getBytes());
        ImportStatus status = new ImportStatus();
        ImportTask task = new ImportTask("123", mockStream, status);

        assertThat(task.getImportId()).isEqualTo("123");
        assertThat(task.getInputStream()).isSameAs(mockStream);
        assertThat(task.getStatus()).isSameAs(status);
    }

    private String extractImportId(String response) throws Exception {
        return new ObjectMapper()
                .readTree(response)
                .get("importId")
                .asText();
    }
}
