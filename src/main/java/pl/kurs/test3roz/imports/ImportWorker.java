package pl.kurs.test3roz.imports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.kurs.test3roz.imports.models.ImportStatus;
import pl.kurs.test3roz.imports.models.Import;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ImportWorker {

    private final ImportQueue queue;
    private final ImportExecutor executor;
    private final ImportLock lock;
    private final ImportService importService;
    private final ImportTaskRepository taskRepository;

    @Bean
    public ApplicationRunner importWorkerRunner() {
        return args -> {
            importService.resumePendingTasks(queue);
            new Thread(() -> {
                while (true) {
                    ImportTask task;
                    try {
                        task = queue.take();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.debug("Thread interrupted while taking from queue", e);
                        continue;
                    }

                    if (task == null) {
                        log.debug("Received null task, skipping.");
                        continue;
                    }

                    try {
                        Import entity = importService.getStatus(task.getImportId());

                        if (!lock.tryLock()) {
                            log.info("Import ID {} delayed – lock unavailable", entity.getImportId());
                            queue.submit(task);
                            TimeUnit.MILLISECONDS.sleep(200);
                            continue;
                        }

                        log.info("Import ID {} started", entity.getImportId());
                        entity.setStatus(ImportStatus.PROCESSING);
                        entity.setFinishedAt(LocalDateTime.now());
                        taskRepository.save(entity);

                        AtomicLong processedRows = new AtomicLong(0);
                        try (InputStream is = Files.newInputStream(Path.of(task.getFilePath()))) {
                            executor.run(is, processedRows, lock);
                            entity.setProcessedRows(processedRows.get());
                            entity.setStatus(ImportStatus.DONE);
                        } catch (Exception e) {
                            log.error("Import ID {} failed during execution", entity.getImportId(), e);
                            entity.setStatus(ImportStatus.FAILED);
                        } finally {
                            if (entity.getStatus() == ImportStatus.DONE) {
                                try {
                                    Files.deleteIfExists(Path.of(task.getFilePath()));
                                } catch (IOException ex) {
                                    log.warn("Failed to delete temp file: {}", task.getFilePath(), ex);
                                }
                            }
                            entity.setFinishedAt(LocalDateTime.now());
                            taskRepository.save(entity);
                            lock.unlock();
                        }

                    } catch (Exception e) {
                        log.error("Unexpected error during import task processing", e);
                    }
                }
            }).start();
        };
    }
}

