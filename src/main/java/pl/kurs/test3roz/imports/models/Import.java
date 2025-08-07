package pl.kurs.test3roz.imports.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "imports")
@Getter
@Setter
@NoArgsConstructor
public class Import {

    @Id
    private String importId;

    private String filePath;

    @Enumerated(EnumType.STRING)
    private ImportStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private long processedRows;
}