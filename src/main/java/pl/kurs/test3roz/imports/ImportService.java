package pl.kurs.test3roz.imports;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportQueue importQueue;
    private final Map<String, ImportStatus> statusMap = new ConcurrentHashMap<>();

    public ImportIdDto importCsv(MultipartFile file) {
        try {
            String importId = UUID.randomUUID().toString();
            ImportStatus status = new ImportStatus();
            status.setId(importId);
            status.setStartedAt(LocalDateTime.now());
            status.setRunning(true);

            statusMap.put(importId, status);
            InputStream inputStream = file.getInputStream();

            ImportTask task = new ImportTask(importId, inputStream, status);
            importQueue.submit(task);

            return new ImportIdDto(importId);
        } catch (Exception e) {
            throw new RuntimeException("Import failed", e);
        }
    }

    public ImportStatus getStatus(String importId) {
        ImportStatus status = statusMap.get(importId);
        if (status == null)
            throw new IllegalArgumentException("Import with id " + importId + " not found");
        return status;
    }
}
