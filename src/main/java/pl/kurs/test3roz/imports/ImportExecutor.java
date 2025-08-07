package pl.kurs.test3roz.imports;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class ImportExecutor {

    private final PersonFileImporter importer;

    @Transactional
    public void run(InputStream inputStream, AtomicLong processedRows, ImportLock lock) {
        importer.run(inputStream, processedRows);
    }
}