package pl.kurs.test3roz.imports;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.test3roz.exceptions.ImportAlreadyRunningException;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportStatus status;
    private final ImportLock lock;
    private final ImportProperties properties;
    private final ExecutorService executorService;
    private final ImportExecutor importExecutor;

    public void importCsv(MultipartFile file) {
        validateLock();
        lock.tryLock();

        try {
            InputStream inputStream = file.getInputStream();
            prepareStatusForStart();
            startAsyncImport(inputStream);
        } catch (Exception e) {
            markStatusAsFailed();
            lock.unlock();
        }
    }

    public ImportStatus getStatus() {
        return status;
    }

    private void validateLock() {
        if (!properties.isAllowParallelImports() && lock.isLocked())
            throw new ImportAlreadyRunningException("Import already in progress");

        if (!lock.tryLock())
            throw new ImportAlreadyRunningException("Could not acquire import lock");
    }

    private void prepareStatusForStart() {
        status.reset();
        status.setStartedAt(LocalDateTime.now());
        status.setRunning(true);
    }

    private void markStatusAsFailed() {
        status.setFailed(true);
        status.setRunning(false);
    }

    private void startAsyncImport(InputStream inputStream) {
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
