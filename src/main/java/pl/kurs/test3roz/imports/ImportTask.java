package pl.kurs.test3roz.imports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ImportTask implements Serializable {
    private String importId;
    private String filePath;
}
