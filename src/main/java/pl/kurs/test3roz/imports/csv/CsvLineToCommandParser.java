package pl.kurs.test3roz.imports.csv;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.exceptions.ImportParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CsvLineToCommandParser {

    private final ObjectMapper objectMapper;
    private final Map<String, List<String>> csvPersonTypeSpecificFields;


    public CreatePersonCommand parse(String line) {
        String[] requiredCommonFields = line.split(",");
        if (requiredCommonFields.length < 8) {
            throw new ImportParseException("CSV line has too few common fields: " + line, null);
        }

        String type = requiredCommonFields[0].trim().toUpperCase();

        Map<String, String> data = new HashMap<>();
        data.put("type", type);
        data.put("firstName", requiredCommonFields[1].trim());
        data.put("lastName", requiredCommonFields[2].trim());
        data.put("pesel", requiredCommonFields[3].trim());
        data.put("height", requiredCommonFields[4].trim());
        data.put("weight", requiredCommonFields[5].trim());
        data.put("email", requiredCommonFields[6].trim());
        data.put("gender", requiredCommonFields[7].trim());
        data.put("password", requiredCommonFields[8].trim());

        List<String> specificFields = csvPersonTypeSpecificFields.get(type);

        if (specificFields == null) {
            throw new ImportParseException("Unknown or unconfigured person type: " + type, null);
        }

        if (requiredCommonFields.length < (9 + specificFields.size())) {
            throw new ImportParseException("CSV line for type " + type + " has too few fields including password: " + line, null);
        }

        for (int i = 0; i < specificFields.size(); i++) {
            data.put(specificFields.get(i), requiredCommonFields[8 + i].trim());
        }

        try {
            return objectMapper.convertValue(data, CreatePersonCommand.class);
        } catch (Exception e) {
            throw new ImportParseException("Failed to convert CSV line to command for type " + type + ": " + e.getMessage(), e);
        }
    }
}
