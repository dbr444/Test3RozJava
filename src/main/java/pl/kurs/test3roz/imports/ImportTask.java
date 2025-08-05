package pl.kurs.test3roz.imports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.io.InputStream;

@Getter
@AllArgsConstructor
public class ImportTask {
    private final String importId;
    private final InputStream inputStream;
    private final ImportStatus status;
}
