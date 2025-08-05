package pl.kurs.test3roz.imports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ImportWorker {

    private final ImportQueue queue;
    private final ImportExecutor executor;
    private final ImportLock lock;

    @Bean
    public ApplicationRunner importWorkerRunner() {
        return args -> {
            new Thread(() -> {
                while (true) {
                    ImportTask task = null;
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
                        ImportStatus status = task.getStatus();

                        if (!lock.tryLock()) {
                            log.info("Import ID {} delayed – lock unavailable", status.getId());
                            queue.submit(task);
                            TimeUnit.MILLISECONDS.sleep(200);
                            continue;
                        }

                        log.info("Import ID {} started", status.getId());
                        status.setRunning(true);
                        status.setStartedAt(LocalDateTime.now());

                        try {
                            executor.run(task.getInputStream(), status, lock);
                            status.setFailed(false);
                        } catch (Exception e) {
                            log.error("Import ID {} failed during execution", status.getId(), e);
                            status.setFailed(true);
                        } finally {
                            status.setRunning(false);
                            status.setFinishedAt(LocalDateTime.now());
                            lock.unlock();
                        }

                    } catch (Exception e) {//nie rzucam bleedem zeby nie przerywac petli - daj znac czy logi sa w porzadku ogolnie
                        log.error("Unexpected error during import task processing", e);
                    }
                }
            }).start();
        };
    }
}
