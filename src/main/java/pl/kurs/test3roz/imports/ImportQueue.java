package pl.kurs.test3roz.imports;

import org.springframework.stereotype.Component;
import pl.kurs.test3roz.exceptions.ImportException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class ImportQueue {

    private final BlockingQueue<ImportTask> queue = new LinkedBlockingQueue<>();

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
