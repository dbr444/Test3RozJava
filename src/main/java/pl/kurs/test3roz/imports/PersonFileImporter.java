package pl.kurs.test3roz.imports;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.imports.csv.CsvLineToCommandParser;
import pl.kurs.test3roz.imports.csv.CsvReader;
import pl.kurs.test3roz.services.PersonService;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class PersonFileImporter {

    private final CsvReader csvReader;
    private final CsvLineToCommandParser parser;
    private final PersonService personService;
    private final ImportProperties properties;

    @Transactional
    public void run(InputStream inputStream, ImportStatus status, ImportLock lock) {
        try (Stream<String[]> stream = csvReader.read(inputStream)) {
            List<CreatePersonCommand> batch = new ArrayList<>();
            final int batchSize = properties.getBatchSize();
            stream.forEach(fields -> {
                CreatePersonCommand createPersonCommand = parser.parse(String.join(",", fields));
                batch.add(createPersonCommand);
                status.getProcessedRows().incrementAndGet();

                if (batch.size() == batchSize) {
                    personService.createAll(batch);
                    batch.clear();
                }
            });
            if (!batch.isEmpty()) {
                personService.createAll(batch);
            }
        } catch (Exception e) {
            status.setFailed(true);
            status.setFinishedAt(LocalDateTime.now());
            status.setRunning(false);
            e.printStackTrace();
            throw e;
        } finally {
            lock.unlock();
        }
    }
}