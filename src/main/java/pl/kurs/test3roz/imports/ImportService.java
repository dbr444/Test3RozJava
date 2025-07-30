package pl.kurs.test3roz.imports;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.test3roz.exceptions.ImportAlreadyRunningException;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportLock lock;
    private final ImportProperties properties;
    private final ExecutorService executorService;
    private final ImportExecutor importExecutor;

    private final Map<String, ImportStatus> statusMap = new ConcurrentHashMap<>();

    public String importCsv(MultipartFile file) {
        validateLock();
        lock.tryLock();

        try {
            InputStream inputStream = file.getInputStream();
            String importId = UUID.randomUUID().toString();

            ImportStatus status = new ImportStatus();
            status.setId(importId);
            status.setStartedAt(LocalDateTime.now());
            status.setRunning(true);
            statusMap.put(importId, status);

            startAsyncImport(inputStream, status);
            return importId;
        } catch (Exception e) {
            lock.unlock();
            throw new RuntimeException("Import failed", e);
        }
    }

    public ImportStatus getStatus(String importId) {
        ImportStatus status = statusMap.get(importId);
        if (status == null)
            throw new IllegalArgumentException("Import with id " + importId + " not found");
        return status;
    }

    private void validateLock() {
        if (!properties.isAllowParallelImports() && lock.isLocked())
            throw new ImportAlreadyRunningException("Import already in progress");

        if (!lock.tryLock())
            throw new ImportAlreadyRunningException("Could not acquire import lock");
    }

    private void startAsyncImport(InputStream inputStream, ImportStatus status) {
        executorService.submit(() -> {
            try {
                importExecutor.run(inputStream, status, lock);
            } catch (Exception e) {
                status.setFailed(true);
            } finally {
                status.setFinishedAt(LocalDateTime.now());
                status.setRunning(false);
            }
        });
    }
}
