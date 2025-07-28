package pl.kurs.test3roz.imports;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.exceptions.ImportParseException;
import pl.kurs.test3roz.imports.csv.CsvLineToCommandParser;
import pl.kurs.test3roz.models.people.Person;
import pl.kurs.test3roz.services.PersonService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class PersonFileImporter {

    private final CsvLineToCommandParser parser;
    private final PersonService personService;
    private final ImportProperties properties;
    private final EntityManager entityManager;

    public void run(InputStream inputStream, ImportStatus status, ImportLock lock) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String[] header = readHeader(reader);
            Stream<String[]> lines = readLines(reader);

            processLines(lines, header, status);
        } catch (Exception e) {
            status.setFailed(true);
            System.out.println(">>> IMPORT ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new ImportParseException("Import failed", e);
        } finally {
            lock.unlock();
        }
    }

    private String[] readHeader(BufferedReader reader) throws Exception {
        return reader.readLine().split(",");
    }

    private Stream<String[]> readLines(BufferedReader reader) {
        return reader.lines().map(line -> line.split(","));
    }

    private void processLines(Stream<String[]> lines, String[] header, ImportStatus status) {
        int batchSize = properties.getBatchSize();
        List<CreatePersonCommand> batch = new ArrayList<>(batchSize);

        lines.forEach(fields -> {
            CreatePersonCommand command = parser.parse(fields, header);
            batch.add(command);
            status.getProcessedRows().incrementAndGet();

            if (batch.size() == batchSize) {
                persistBatch(batch);
                batch.clear();
            }
        });

        if (!batch.isEmpty())
            persistBatch(batch);
    }

    private void persistBatch(List<CreatePersonCommand> batch) {
        batch.forEach(command -> {
            Person person = personService.preparePersonForImport(command);
            entityManager.persist(person);
        });
        entityManager.flush();
        entityManager.clear();
    }
}
