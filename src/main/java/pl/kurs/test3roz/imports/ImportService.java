package pl.kurs.test3roz.imports;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.test3roz.imports.models.ImportIdDto;
import pl.kurs.test3roz.imports.models.ImportStatusDto;
import pl.kurs.test3roz.imports.models.ImportStatus;
import pl.kurs.test3roz.imports.models.Import;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportQueue importQueue;
    private final ImportProperties properties;
    private final ImportTaskRepository taskRepository;

    public ImportIdDto importCsv(MultipartFile file) {
        try {
            String importId = UUID.randomUUID().toString();

            Path tempDir = Path.of(properties.getTempDir());
            Files.createDirectories(tempDir);
            Path tempFile = tempDir.resolve(importId + ".csv");
            file.transferTo(tempFile.toFile());

            Import entity = new Import();
            entity.setImportId(importId);
            entity.setFilePath(tempFile.toString());
            entity.setStatus(ImportStatus.PENDING);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setFinishedAt(LocalDateTime.now());
            taskRepository.save(entity);

            ImportTask task = new ImportTask(importId, tempFile.toString());
            importQueue.submit(task);

            return new ImportIdDto(importId);
        } catch (Exception e) {
            throw new RuntimeException("Import failed", e);
        }
    }

    public Import getStatus(String importId) {
        return taskRepository.findById(importId)
                .orElseThrow(() -> new IllegalArgumentException("Import with id " + importId + " not found"));
    }

    public ImportStatusDto getStatusDto(String importId) {
        Import entity = taskRepository.findById(importId)
                .orElseThrow(() -> new IllegalArgumentException("Import with id " + importId + " not found"));
        return new ImportStatusDto(entity.getImportId(), entity.getStatus().name(), entity.getCreatedAt(), entity.getFinishedAt(), entity.getProcessedRows(), entity.getStatus() == ImportStatus.FAILED, entity.getStatus() == ImportStatus.PROCESSING);
    }

    public void resumePendingTasks(ImportQueue queue) {
        List<Import> pendingTasks = taskRepository.findAllByStatusIn(List.of(ImportStatus.PENDING, ImportStatus.PROCESSING));
        for (Import taskEntity : pendingTasks) {
            ImportTask task = new ImportTask(taskEntity.getImportId(), taskEntity.getFilePath());
            queue.submit(task);
        }
    }
}
