package pl.kurs.test3roz.imports;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class ImportExecutor {

    private final PersonFileImporter importer;

    @Transactional
    public void run(InputStream inputStream, ImportStatus status, ImportLock lock) {
        importer.run(inputStream, status, lock);
    }
}