package pl.kurs.test3roz.imports.csv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CsvLineToCommandParser {

    private final Map<String, Class<? extends CreatePersonCommand>> personCommandTypeMap;
    private final ObjectMapper objectMapper;

    public CreatePersonCommand parse(String[] values, String[] header) {
        String type = values[0].toUpperCase();
        Class<? extends CreatePersonCommand> clazz = personCommandTypeMap.get(type);
        if (clazz == null)
            throw new IllegalArgumentException("Unknown type: " + type);

        ObjectNode node = objectMapper.createObjectNode();
        for (int i = 0; i < header.length; i++) {
            node.put(header[i], values[i]);
        }

        try {
            return objectMapper.treeToValue(node, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to map CSV line to " + clazz.getSimpleName(), e);
        }
    }
}
