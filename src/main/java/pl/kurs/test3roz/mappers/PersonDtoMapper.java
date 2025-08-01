package pl.kurs.test3roz.mappers;

import org.springframework.stereotype.Component;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.models.people.Person;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PersonDtoMapper {

    private final Map<Class<? extends Person>, IPersonDtoMapper<? extends Person>> mappers;

    public PersonDtoMapper(List<IPersonDtoMapper<?>> mappers) {
        this.mappers = mappers.stream()
                .collect(Collectors.toMap(IPersonDtoMapper::getSupportedClass, Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Person> PersonDto map(T person) {
        IPersonDtoMapper<T> mapper = (IPersonDtoMapper<T>) mappers.get(person.getClass());
        if (mapper == null)
            throw new IllegalStateException("No mapper found for class " + person.getClass().getSimpleName());
        return mapper.map(person);
    }

    @SuppressWarnings("unchecked")
    public <T extends Person> void applyTypeSpecificData(T person, CreatePersonCommand command) {
        IPersonDtoMapper<T> mapper = (IPersonDtoMapper<T>) mappers.get(person.getClass());
        if (mapper == null)
            throw new IllegalStateException("No mapper found for class " + person.getClass().getSimpleName());
        mapper.applyTypeSpecificData(person, command);
    }
}

