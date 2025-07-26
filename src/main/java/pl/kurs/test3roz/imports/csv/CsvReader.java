package pl.kurs.test3roz.imports.csv;

import org.springframework.stereotype.Component;
import pl.kurs.test3roz.exceptions.ImportParseException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

@Component
public class CsvReader {

    public Stream<String[]> read(InputStream inputStream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return reader.lines()
                    .skip(1) //naglowek skip
                    .map(line -> line.split(","));
        } catch (Exception e) {
            throw new ImportParseException("Failed to read CSV file", e);
        }
    }
}