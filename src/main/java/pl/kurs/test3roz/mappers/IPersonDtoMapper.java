package pl.kurs.test3roz.mappers;

import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.models.people.Person;

public interface IPersonDtoMapper<T extends Person> {
    PersonDto map(T person);
    default void applyTypeSpecificData(Person person, CreatePersonCommand command) {
    }
    Class<T> getSupportedClass();
}
