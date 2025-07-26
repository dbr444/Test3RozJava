package pl.kurs.test3roz.imports;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Getter
@Setter
public class ImportStatus {
    private volatile boolean running = false;
    private volatile boolean failed = false;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private final AtomicLong processedRows = new AtomicLong(0);

    public void reset() {
        running = false;
        failed = false;
        startedAt = null;
        finishedAt = null;
        processedRows.set(0);
    }
}