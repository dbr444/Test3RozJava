package pl.kurs.test3roz.imports;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.exceptions.ImportException;

@Component
public class ImportQueue {

    private final RBlockingQueue<ImportTask> queue;

    public ImportQueue(RedissonClient redissonClient) {
        this.queue = redissonClient.getBlockingQueue("import_queue");
    }

    public void submit(ImportTask task) {
        try {
            queue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ImportException("Interrupted while submitting import task");
        }
    }

    public ImportTask take() throws InterruptedException {
        return queue.take();
    }
}
