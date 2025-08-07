package pl.kurs.test3roz.imports.models;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class ImportStatusDto {
    private String importId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long processedRows;
    private boolean failed;
    private boolean running;
}

