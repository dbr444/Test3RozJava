package pl.kurs.test3roz.controllers;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.test3roz.commands.CreateEmployeeCommand;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.commands.CreateStudentCommand;
import pl.kurs.test3roz.commands.UpdateStudentCommand;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.dto.StudentDto;
import pl.kurs.test3roz.exceptions.IllegalEntityIdException;
import pl.kurs.test3roz.exceptions.IllegalEntityStateException;
import pl.kurs.test3roz.exceptions.RequestedEntityNotFoundException;
import pl.kurs.test3roz.imports.ImportStatus;
import pl.kurs.test3roz.models.Gender;
import pl.kurs.test3roz.models.IdGenerator;
import pl.kurs.test3roz.models.Position;
import pl.kurs.test3roz.models.people.Employee;
import pl.kurs.test3roz.models.people.Person;
import pl.kurs.test3roz.services.PersonService;
import pl.kurs.test3roz.services.crudservices.EmployeeCrudService;
import pl.kurs.test3roz.services.crudservices.PersonCrudService;
import pl.kurs.test3roz.services.crudservices.PositionCrudService;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonCrudService personCrudService;

    @Autowired
    PersonService personService;

    @Autowired
    private EmployeeCrudService employeeCrudService;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private ImportStatus importStatus;

    @Autowired
    private PositionCrudService positionCrudService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        positionCrudService.deleteAllEntities();
        personCrudService.deleteAllEntities();
        idGenerator.getEm()
                .createNativeQuery("INSERT INTO id_sequence (entity_name, next_val) VALUES ('person', 1)")
                .executeUpdate();
        employee = new Employee();
        employee.setFirstName("Jan");
        employee.setLastName("Kowalski");
        employee.setPesel("12345678901");
        employee.setEmail("janek.kowal@test.pl");
        employee.setHeight(180.5);
        employee.setWeight(80.0);
        employee.setGender(Gender.MALE);
        employee.setPassword("password");
        employee.setId(idGenerator.nextId());
        setTypeFromAnnotation(employee);
        employeeCrudService.addWithManualId(employee);
    }

    private void setTypeFromAnnotation(Employee e) {
        var annotation = e.getClass().getAnnotation(pl.kurs.test3roz.models.PersonType.class);
        if (annotation != null) {
            e.setType(annotation.value());
        } else {
            throw new IllegalStateException("Missing @PersonType annotation on class " + e.getClass().getSimpleName());
        }
    }

    @Test
    void shouldCreatePersonSuccessfully() throws Exception {
        String requestBody = Files.readString(Path.of("src/test/resources/json/create-valid-employee.json"));

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Janek"))
                .andExpect(jsonPath("$.email").value("janek.nowy@test.pl"));
    }

    @Test
    void shouldUpdatePersonSuccessfully() throws Exception {
        String updateBody = Files.readString(Path.of("src/test/resources/json/update-valid-employee.json"));

        mockMvc.perform(put("/api/persons/" + employee.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Janek"))
                .andExpect(jsonPath("$.lastName").value("Kowal"))
                .andExpect(jsonPath("$.email").value("janek.kowal@test.pl"));

        Person updated = personCrudService.get(employee.getId());
        assertThat(updated.getFirstName()).isEqualTo("Janek");
        assertThat(updated.getLastName()).isEqualTo("Kowal");
        assertThat(updated.getEmail()).isEqualTo("janek.kowal@test.pl");
        assertThat(updated.getHeight()).isEqualTo(185.0);
        assertThat(updated.getWeight()).isEqualTo(85.0);
    }

    @Test
    void shouldReturnFilteredPersons() throws Exception {
        String json = """
                {
                  "firstName": "Jan"
                }
                """;

        mockMvc.perform(post("/api/persons/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Janek"))
                .andExpect(jsonPath("$.content[0].lastName").value("Kowal"));
    }

    @Test
    void shouldReturnPersonById() {
        Person p = personCrudService.get(employee.getId());
        assertThat(p).isNotNull();
        assertThat(p.getId()).isEqualTo(employee.getId());
    }

    @Test
    void shouldCreateMultiplePersons() {
        Employee e1 = new Employee();
        e1.setFirstName("A");
        e1.setLastName("B");
        e1.setPesel("02070803629");
        e1.setEmail("a@b.pl");
        e1.setHeight(170.0);
        e1.setWeight(70.0);
        e1.setGender(Gender.MALE);
        e1.setPassword("pass");
        setTypeFromAnnotation(e1);
        e1.setId(idGenerator.nextId());

        Employee e2 = new Employee();
        e2.setFirstName("C");
        e2.setLastName("D");
        e2.setPesel("02070803630");
        e2.setEmail("c@d.pl");
        e2.setHeight(160.0);
        e2.setWeight(60.0);
        e2.setGender(Gender.FEMALE);
        e2.setPassword("pass");
        setTypeFromAnnotation(e2);
        e2.setId(idGenerator.nextId());

        employeeCrudService.addWithManualId(e1);
        employeeCrudService.addWithManualId(e2);

        List<Person> all = personCrudService.getAll();
        assertThat(all).hasSize(3);
    }

    @Test
    void shouldHaveNoActivePositionInitially() {
        boolean hasActive = employee.getPositions().stream()
                .anyMatch(p -> p.getEndDate() == null);
        assertThat(hasActive).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenEndDateIsBeforeHireDate() {
        CreateEmployeeCommand cmd = new CreateEmployeeCommand();
        cmd.setFirstName("A");
        cmd.setLastName("B");
        cmd.setPesel("44051401359");
        cmd.setHeight(180.0);
        cmd.setWeight(80.0);
        cmd.setGender(Gender.MALE);
        cmd.setEmail("a@b.pl");
        cmd.setPassword("abc");
        cmd.setCurrentPosition("Tester");
        cmd.setCurrentSalary(BigDecimal.valueOf(10000));
        cmd.setHireDate(LocalDate.of(2024, 2, 2));
        cmd.setEndDate(LocalDate.of(2023, 2, 2));

        assertThatThrownBy(() -> personService.createPerson(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("End date must not be before start date.");
    }

    @Test
    void shouldReturnNullForLastPositionWhenEmployeeHasNoPositions() throws Exception {
        Employee e = new Employee();

        Method method = PersonService.class.getDeclaredMethod("getLastPosition", Employee.class);
        method.setAccessible(true);
        Position p = (Position) method.invoke(personService, e);

        assertThat(p).isNull();
    }

    @Test
    void shouldCreateMultiplePersonsSuccessfully() {
        CreateEmployeeCommand cmd1 = new CreateEmployeeCommand();
        cmd1.setFirstName("Anna");
        cmd1.setLastName("Kowalska");
        cmd1.setEmail("anna1@example.com");
        cmd1.setPesel("90010112345");
        cmd1.setPassword("pass123");
        cmd1.setGender(Gender.FEMALE);
        cmd1.setHeight(165.0);
        cmd1.setWeight(60.0);
        cmd1.setCurrentPosition("Developer");
        cmd1.setCurrentSalary(BigDecimal.valueOf(8000));
        cmd1.setHireDate(LocalDate.now().minusYears(1));
        cmd1.setEndDate(LocalDate.now());

        CreateEmployeeCommand cmd2 = new CreateEmployeeCommand();
        cmd2.setFirstName("Jan");
        cmd2.setLastName("Nowak");
        cmd2.setEmail("jan1@example.com");
        cmd2.setPesel("85030312345");
        cmd2.setPassword("pass123");
        cmd2.setGender(Gender.MALE);
        cmd2.setHeight(180.0);
        cmd2.setWeight(80.0);
        cmd2.setCurrentPosition("Manager");
        cmd2.setCurrentSalary(BigDecimal.valueOf(12000));
        cmd2.setHireDate(LocalDate.now().minusYears(2));
        cmd2.setEndDate(LocalDate.now());

        List<CreatePersonCommand> commands = List.of(cmd1, cmd2);

        personService.createAll(commands);

        List<Person> allPersons = personCrudService.getAll();
        assertThat(allPersons).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldCreateStudentAndReturnStudentDto() {
        CreateStudentCommand cmd = createValidStudentCommand();

        PersonDto dto = personService.createPerson(cmd);
        assertThat(dto).isInstanceOf(StudentDto.class);
    }

    @Test
    void shouldThrowOptimisticLockExceptionWhenVersionMismatch() {
        CreateStudentCommand cmd = createValidStudentCommand();

        PersonDto created = personService.createPerson(cmd);

        UpdateStudentCommand update = new UpdateStudentCommand();
        update.setFirstName("Ala");
        update.setLastName("Nowak");
        update.setGender(Gender.FEMALE);
        update.setHeight(160.0);
        update.setWeight(55.0);
        update.setStudyMajor("Matematyka");
        update.setVersion(Long.valueOf(created.getVersion() + 1));

        assertThatThrownBy(() -> personService.updatePerson(created.getId(), update))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void shouldThrowWhenMissingPersonTypeAnnotation() {
        CommandWithoutAnnotation cmd = new CommandWithoutAnnotation();
        cmd.setFirstName("Jan");
        cmd.setLastName("Kowalski");
        cmd.setGender(Gender.MALE);
        cmd.setHeight(180.0);
        cmd.setWeight(75.0);
        cmd.setPassword("test123");

        assertThatThrownBy(() -> personService.createPerson(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing @PersonType annotation");
    }

    @Test
    void shouldExposeEntityTypeFromRequestedEntityNotFoundException() {
        RequestedEntityNotFoundException ex = new RequestedEntityNotFoundException("not found", Employee.class);
        assertThat(ex.getEntityType()).isEqualTo(Employee.class);
    }

    @Test
    void shouldThrowWhenGettingNonExistentEmployeeById() {
        Long nonExistentId = 99999L;

        assertThatThrownBy(() -> employeeCrudService.get(nonExistentId))
                .isInstanceOf(RequestedEntityNotFoundException.class)
                .hasMessageContaining("Entity with id " + nonExistentId + " not found!");
    }

    @Test
    void shouldThrowWhenAddingEmployeeWithManualIdButIdIsNull() {
        Employee employee = new Employee();
        employee.setId(null);

        assertThatThrownBy(() -> employeeCrudService.addWithManualId(employee))
                .isInstanceOf(IllegalEntityStateException.class)
                .satisfies(e -> {
                    IllegalEntityStateException ex = (IllegalEntityStateException) e;
                    assertThat(ex.getEntityType()).isEqualTo(Employee.class);
                });
    }

    @Test
    void shouldThrowWhenGettingWithNullId() {
        assertThatThrownBy(() -> employeeCrudService.get(null))
                .isInstanceOf(IllegalEntityIdException.class)
                .satisfies(e -> {
                    IllegalEntityIdException ex = (IllegalEntityIdException) e;
                    assertThat(ex.getEntityType()).isEqualTo(Employee.class);
                });
    }

    @Test
    void shouldThrowRequestedEntityNotFoundWhenFindByIdWithLockNotExists() {
        Long nonExistentId = 9999L;

        assertThatThrownBy(() -> employeeCrudService.findByIdWithLock(nonExistentId))
                .isInstanceOf(RequestedEntityNotFoundException.class)
                .hasMessageContaining("Entity not found!");
    }

    @Test
    void shouldThrowWhenAddingWithNonNullId() {
        Employee e = new Employee();
        e.setId(123L);

        assertThatThrownBy(() -> employeeCrudService.add(e))
                .isInstanceOf(IllegalEntityStateException.class)
                .hasMessageContaining("Entity ID should be null before persisting!");
    }

    @Test
    void shouldThrowWhenAddingListWithManualIdButOneHasNullId() {
        Employee e1 = new Employee();
        e1.setId(123L);

        Employee e2 = new Employee();
        e2.setId(null);

        List<Employee> list = List.of(e1, e2);

        assertThatThrownBy(() -> employeeCrudService.addAllWithManualId(list))
                .isInstanceOf(IllegalEntityStateException.class)
                .hasMessageContaining("Entity ID must be set before persisting!");
    }

    private CreateStudentCommand createValidStudentCommand() {
        CreateStudentCommand cmd = new CreateStudentCommand();
        cmd.setFirstName("Anna");
        cmd.setLastName("Nowak");
        cmd.setGender(Gender.FEMALE);
        cmd.setPesel("02070803628");
        cmd.setEmail("anna@test.com");
        cmd.setHeight(160.0);
        cmd.setWeight(55.0);
        cmd.setPassword("pass123");
        cmd.setCurrentUniversityName("UW");
        cmd.setStudyMajor("Informatyka");
        return cmd;
    }


    static class NoAnnotationPerson extends Person {
        public NoAnnotationPerson() {
        }
    }

    static class CommandWithoutAnnotation extends CreatePersonCommand {
        @Override
        public Class<? extends Person> getTargetClass() {
            return NoAnnotationPerson.class;
        }

        @Override
        public Class<? extends PersonDto> getTargetDtoClass() {
            return PersonDto.class;
        }
    }
}
