package pl.kurs.test3roz.controllers;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.test3roz.imports.ImportLock;
import pl.kurs.test3roz.imports.ImportProperties;
import pl.kurs.test3roz.imports.ImportService;
import pl.kurs.test3roz.imports.ImportStatus;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

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
    private ImportStatus importStatus;

    @Autowired
    private ImportLock importLock;

    @Autowired
    private ImportProperties importProperties;

    @BeforeEach
    void setUp() {
        importStatus.reset();
        importLock.unlock();
    }

    @Test
    void shouldStartCsvImport() throws Exception {
        byte[] content = Files.readAllBytes(Paths.get("src/test/resources/test-import.csv"));
        MockMultipartFile file = new MockMultipartFile("file", "test-import.csv", "text/csv", content);

        mockMvc.perform(multipart("/api/imports").file(file))
                .andExpect(status().isAccepted());

        await().atMost(2, SECONDS).untilAsserted(() ->
                mockMvc.perform(get("/api/imports/status").with(user("test").roles("IMPORTER")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.running").value(false))
        );
    }

    @Test
    void shouldReturnStatusWithoutStartingImport() throws Exception {
        mockMvc.perform(get("/api/imports/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(false))
                .andExpect(jsonPath("$.processedRows").value(0));
    }

    @Test
    void shouldReturn409WhenCannotAcquireLock() throws Exception {
        Field lockField = ImportLock.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        ((java.util.concurrent.atomic.AtomicBoolean) lockField.get(importLock)).set(true);

        mockMvc.perform(multipart("/api/imports")
                        .file(new MockMultipartFile("file", "test.csv", "text/csv", "type,firstName\n".getBytes()))
                        .with(user("admin").roles("IMPORTER")))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldStartImportButFailAndSetStatusAccordingly() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv",
                "type,firstName,lastName,pesel,height,weight,email,gender\nEMPLOYEE,A,B,INVALID_PESEL,180,75,a@b.pl,MALE".getBytes()
        );

        mockMvc.perform(multipart("/api/imports").file(file))
                .andExpect(status().isAccepted());

        Thread.sleep(500);

        mockMvc.perform(get("/api/imports/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.failed").value(true))
                .andExpect(jsonPath("$.running").value(false));
    }

    @Test
    void shouldReturn409WhenImportAlreadyRunningAndParallelNotAllowed() throws Exception {
        importProperties.setAllowParallelImports(false);
        importLock.tryLock();

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv",
                "type,firstName,lastName,pesel,height,weight,email,gender,password\nEMPLOYEE,A,B,12345678901,180,75,a@b.pl,MALE,pass".getBytes()
        );

        mockMvc.perform(multipart("/api/imports").file(file))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Import already in progress")));

        importLock.unlock();
    }

    @Test
    void shouldReturn409WhenCannotAcquireLockEvenIfParallelAllowed() throws Exception {
        importProperties.setAllowParallelImports(true);
        importLock.tryLock();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                """
                    type,firstName,lastName,pesel,height,weight,email,gender,password
                    EMPLOYEE,A,B,12345678901,180,75,a@b.pl,MALE,pass
                    """.getBytes()
        );

        mockMvc.perform(multipart("/api/imports").file(file))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Could not acquire import lock")));

        importLock.unlock();
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

        mockMvc.perform(multipart("/api/imports").file(brokenFile))
                .andExpect(status().isAccepted());

        Thread.sleep(500);

        mockMvc.perform(get("/api/imports/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.failed").value(true))
                .andExpect(jsonPath("$.running").value(false));

        assertThat(importLock.isLocked()).isFalse();
    }

    @Test
    void shouldSetFailedWhenTooFewCommonFields() throws Exception {
        String content = """
        type,firstName
        EMPLOYEE,John
        """;

        MockMultipartFile file = new MockMultipartFile("file", "bad.csv", "text/csv", content.getBytes());

        mockMvc.perform(multipart("/api/imports").file(file))
                .andExpect(status().isAccepted());

        Thread.sleep(500);

        mockMvc.perform(get("/api/imports/status"))
                .andExpect(jsonPath("$.failed").value(true));
    }

    @Test
    void shouldFailImportWhenUnknownTypeProvided() throws Exception {
        String content = """
            type,firstName,lastName,pesel,height,weight,email,salary
            xxxxx,John,Doe,12345678901,180,75,john@example.com,10000
            """;

        MockMultipartFile file = new MockMultipartFile("file", "people.csv", "text/csv", content.getBytes());

        mockMvc.perform(multipart("/api/imports").file(file)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isAccepted());

        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            mockMvc.perform(get("/api/imports/status")
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

        mockMvc.perform(multipart("/api/imports").file(file)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isAccepted());

        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            mockMvc.perform(get("/api/imports/status")
                            .with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.failed").value(true))
                    .andExpect(jsonPath("$.running").value(false));
        });
    }

    @Test
    void shouldSetFailedAndUnlockWhenExceptionOccursInImportCsvBeforeAsync() throws Exception {
        MultipartFile faultyFile = mock(MultipartFile.class);
        when(faultyFile.getInputStream()).thenThrow(new RuntimeException("Simulated getInputStream failure"));
        when(faultyFile.getName()).thenReturn("file");
        when(faultyFile.getOriginalFilename()).thenReturn("broken.csv");

        importService.importCsv(faultyFile);

        assertThat(importStatus.isFailed()).isTrue();
        assertThat(importStatus.isRunning()).isFalse();
        assertThat(importLock.isLocked()).isFalse();
    }
}
