package pl.kurs.test3roz.services;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kurs.test3roz.commands.CreateEmployeeCommand;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.commands.UpdatePersonCommand;
import pl.kurs.test3roz.dto.EmployeeDto;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.filters.GetPersonFilter;
import pl.kurs.test3roz.models.IdGenerator;
import pl.kurs.test3roz.models.PersonType;
import pl.kurs.test3roz.models.Position;
import pl.kurs.test3roz.models.people.Employee;
import pl.kurs.test3roz.models.people.Person;
import pl.kurs.test3roz.views.PersonSummaryView;
import pl.kurs.test3roz.services.crudservices.PersonCrudService;
import pl.kurs.test3roz.services.crudservices.PersonSummaryViewCrudService;
import java.util.List;
import java.util.stream.IntStream;

@Service
@Transactional
@AllArgsConstructor
public class PersonService {

    private final PersonCrudService personCrudService;
    private final ModelMapper modelMapper;
    private final PersonSummaryViewCrudService personSummaryViewCrudService;
    private final PasswordEncoder passwordEncoder;
    private final IdGenerator idGenerator;

    public PersonDto createPerson(CreatePersonCommand command) {
        Person person = preparePerson(command, idGenerator.nextId());
        Person saved = personCrudService.addWithManualId(person);

        if (saved instanceof Employee employee)
            return mapEmployeeToDtoWithLastPosition(employee);

        return modelMapper.map(saved, command.getTargetDtoClass());
    }


    public void createAll(List<CreatePersonCommand> commands) {
        List<Long> ids = idGenerator.nextIds(commands.size());

        List<Person> persons = IntStream.range(0, commands.size())
                .mapToObj(i -> preparePerson(commands.get(i), ids.get(i)))
                .toList();

        personCrudService.addAllWithManualId(persons);
    }

    public PersonDto updatePerson(Long id, UpdatePersonCommand command) {
        Person person = personCrudService.get(id);

        if (!person.getVersion().equals(command.getVersion()))
            throw new OptimisticLockException("Person has been modified concurrently.");

        updateCommonFields(person, command);
        command.applyTo(person);

        if (person instanceof Employee employee)
            return mapEmployeeToDtoWithLastPosition(employee);

        return modelMapper.map(person, command.getTargetDtoClass());
    }


    public Page<PersonSummaryView> getFilteredPersonSummaries(GetPersonFilter filter, Pageable pageable) {
        return personSummaryViewCrudService.findFiltered(filter, pageable);
    }


    // helpers
    private Person preparePerson(CreatePersonCommand command, Long id) {
        Person person = modelMapper.map(command, command.getTargetClass());
        setTypeFromAnnotation(person, command.getTargetClass());
        person.setId(id);
        setEncodedPassword(person, command.getPassword());
        addCurrentPositionIfEmployee(person, command);
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

    private void addCurrentPositionIfEmployee(Person person, CreatePersonCommand command) {
        if (person instanceof Employee employee && command instanceof CreateEmployeeCommand employeeCommand) {
            if (employeeCommand.getEndDate().isBefore(employeeCommand.getHireDate()))
                throw new IllegalStateException("End date must not be before start date.");

            Position p = new Position();
            p.setJobName(employeeCommand.getCurrentPosition());
            p.setStartDate(employeeCommand.getHireDate());
            p.setEndDate(employeeCommand.getEndDate());
            p.setSalary(employeeCommand.getCurrentSalary());
            p.setEmployee(employee);
            employee.getPositions().add(p);
        }
    }

    private EmployeeDto mapEmployeeToDtoWithLastPosition(Employee employee) {
        Position last = getLastPosition(employee);
        EmployeeDto dto = modelMapper.map(employee, EmployeeDto.class);
        if (last != null) {
            dto.setCurrentPosition(last.getJobName());
            dto.setCurrentSalary(last.getSalary());
        }
        return dto;
    }

    private Position getLastPosition(Employee employee) {
        List<Position> positions = employee.getPositions();
        return positions.isEmpty() ? null : positions.get(positions.size() - 1);
    }

    private void updateCommonFields(Person person, UpdatePersonCommand command) {
        person.setFirstName(command.getFirstName());
        person.setLastName(command.getLastName());
        person.setHeight(command.getHeight());
        person.setWeight(command.getWeight());
        person.setGender(command.getGender());
    }
}
