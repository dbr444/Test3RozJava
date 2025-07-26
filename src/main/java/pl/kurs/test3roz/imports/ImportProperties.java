package pl.kurs.test3roz.imports;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "import")
@Getter
@Setter
public class ImportProperties {
    private boolean allowParallelImports;
    private int batchSize;
}