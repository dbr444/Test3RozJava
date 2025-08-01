package pl.kurs.test3roz.services;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.commands.UpdatePersonCommand;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.filters.GetPersonFilter;
import pl.kurs.test3roz.mappers.PersonDtoMapper;
import pl.kurs.test3roz.models.PersonType;
import pl.kurs.test3roz.models.people.Person;
import pl.kurs.test3roz.services.crudservices.PersonCrudService;
import pl.kurs.test3roz.services.crudservices.PersonSummaryViewCrudService;
import pl.kurs.test3roz.views.PersonSummaryView;

@Service
@Transactional
@AllArgsConstructor
public class PersonService {

    private final PersonCrudService personCrudService;
    private final ModelMapper modelMapper;
    private final PersonSummaryViewCrudService personSummaryViewCrudService;
    private final PasswordEncoder passwordEncoder;
    private final PersonDtoMapper personDtoMapper;

    public PersonDto createPerson(CreatePersonCommand command) {
        Person person = preparePerson(command);
        Person saved = personCrudService.add(person);
        return personDtoMapper.map(saved);
    }

    public PersonDto updatePerson(String id, UpdatePersonCommand command) {
        Person person = personCrudService.get(id);

        if (!person.getVersion().equals(command.getVersion()))
            throw new OptimisticLockException("Person has been modified concurrently.");

        updateCommonFields(person, command);
        command.applyTo(person);
        return personDtoMapper.map(person);
    }

    public Page<PersonSummaryView> getFilteredPersonSummaries(GetPersonFilter filter, Pageable pageable) {
        return personSummaryViewCrudService.findFiltered(filter, pageable);
    }

    public Person preparePerson(CreatePersonCommand command) {
        Person person = modelMapper.map(command, command.getTargetClass());
        setTypeFromAnnotation(person, command.getTargetClass());
        setEncodedPassword(person, command.getPassword());
        personDtoMapper.applyTypeSpecificData(person, command);
        return person;
    }

    public Person preparePersonForImport(CreatePersonCommand command) {
        Person person = modelMapper.map(command, command.getTargetClass());
        setTypeFromAnnotation(person, command.getTargetClass());
        personDtoMapper.applyTypeSpecificData(person, command);
        return person;
    }

    private void setTypeFromAnnotation(Person person, Class<?> clazz) {
        PersonType annotation = clazz.getAnnotation(PersonType.class);
        if (annotation == null)
            throw new IllegalStateException("Missing @PersonType annotation on class " + clazz.getSimpleName());
        person.setType(annotation.value());
    }

    private void setEncodedPassword(Person person, String rawPassword) {
        person.setPassword(passwordEncoder.encode(rawPassword));
    }

    private void updateCommonFields(Person person, UpdatePersonCommand command) {
        person.setFirstName(command.getFirstName());
        person.setLastName(command.getLastName());
        person.setHeight(command.getHeight());
        person.setWeight(command.getWeight());
        person.setGender(command.getGender());
    }
}

