package pl.kurs.test3roz.imports;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
public class ImportStatus {
    private final AtomicLong processedRows = new AtomicLong(0);
    private volatile boolean running = false;
    private volatile boolean failed = false;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String id;
}
