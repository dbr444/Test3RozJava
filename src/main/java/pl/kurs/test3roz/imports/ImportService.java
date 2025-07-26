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
    private final PersonFileImporter personFileImporter;
    private final ExecutorService executorService;

    public void importCsv(MultipartFile file) {
        if (!properties.isAllowParallelImports() && lock.isLocked()) {
            throw new ImportAlreadyRunningException("Import already in progress");
        }

        if (!lock.tryLock()) {
            throw new ImportAlreadyRunningException("Could not acquire import lock");
        }

        try {
            status.reset();
            status.setStartedAt(LocalDateTime.now());
            status.setRunning(true);

            InputStream inputStream = file.getInputStream();
            executorService.submit(() -> {
                try {
                    personFileImporter.run(inputStream, status, lock);
                } catch (Exception e) {
                    status.setFailed(true);
                } finally {
                    status.setFinishedAt(LocalDateTime.now());
                    status.setRunning(false);
                }
            });
        } catch (Exception e) {
            status.setFailed(true);
            status.setRunning(false);
            lock.unlock();
        }
    }


    public ImportStatus getStatus() {
        return status;
    }
    }