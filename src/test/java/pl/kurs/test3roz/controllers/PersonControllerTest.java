package pl.kurs.test3roz.controllers;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.commands.CreateRetireeCommand;
import pl.kurs.test3roz.commands.CreateStudentCommand;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.dto.RetireeDto;
import pl.kurs.test3roz.dto.StudentDto;
import pl.kurs.test3roz.exceptions.IllegalEntityIdException;
import pl.kurs.test3roz.exceptions.IllegalEntityStateException;
import pl.kurs.test3roz.exceptions.RequestedEntityNotFoundException;
import pl.kurs.test3roz.mappers.PersonDtoMapper;
import pl.kurs.test3roz.models.Gender;
import pl.kurs.test3roz.models.PersonType;
import pl.kurs.test3roz.models.people.Employee;
import pl.kurs.test3roz.models.people.Person;
import pl.kurs.test3roz.services.PersonService;
import pl.kurs.test3roz.services.crudservices.EmployeeCrudService;
import pl.kurs.test3roz.services.crudservices.PersonCrudService;
import pl.kurs.test3roz.services.crudservices.PositionCrudService;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private PositionCrudService positionCrudService;

    @Autowired
    private PersonDtoMapper personDtoMapper;

    private Employee employee;

    @BeforeEach
    void setUp() {
        positionCrudService.deleteAllEntities();
        personCrudService.deleteAllEntities();

        employee = new Employee();
        employee.setFirstName("Jan");
        employee.setLastName("Kowalski");
        employee.setPesel("12345678901");
        employee.setEmail("janek.kowal@test.pl");
        employee.setHeight(180.5);
        employee.setWeight(80.0);
        employee.setGender(Gender.MALE);
        employee.setPassword("password");
        setTypeFromAnnotation(employee);
        employeeCrudService.add(employee);
    }

    private void setTypeFromAnnotation(Employee e) {
        PersonType annotation = e.getClass().getAnnotation(PersonType.class);
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
        Person person = personCrudService.get(employee.getId());
        assertThat(person).isNotNull();
        assertThat(person.getId()).isEqualTo(employee.getId());
    }

    @Test
    void shouldHaveNoActivePositionInitially() {
        boolean hasActive = employee.getPositions().stream()
                .anyMatch(p -> p.getEndDate() == null);
        assertThat(hasActive).isFalse();
    }

    @Test
    void shouldCreateStudentAndReturnStudentDto() {
        CreateStudentCommand cmd = createValidStudentCommand();

        PersonDto dto = personService.createPerson(cmd);
        assertThat(dto).isInstanceOf(StudentDto.class);
    }

    @Test
    void shouldCreateRetireeAndReturnRetireeDto() {
        CreateRetireeCommand cmd = createValidRetireeCommand();

        PersonDto dto = personService.createPerson(cmd);
        assertThat(dto).isInstanceOf(RetireeDto.class);
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
        String nonExistentId = "9999999999999999";

        assertThatThrownBy(() -> employeeCrudService.get(nonExistentId))
                .isInstanceOf(RequestedEntityNotFoundException.class)
                .hasMessageContaining("Entity with id " + nonExistentId + " not found!");
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
        String nonExistentId = "9999999999999999";

        assertThatThrownBy(() -> employeeCrudService.findByIdWithLock(nonExistentId))
                .isInstanceOf(RequestedEntityNotFoundException.class)
                .hasMessageContaining("Entity not found!");
    }

    @Test
    void shouldThrowRequestedEntityNotFoundWhenGetWithRelationsNotExists() {
        String nonExistentId = "9999999999999999";

        assertThatThrownBy(() -> employeeCrudService.getWithRelations(nonExistentId))
                .isInstanceOf(RequestedEntityNotFoundException.class)
                .hasMessageContaining("Entity not found!");
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

    private CreateRetireeCommand createValidRetireeCommand() {
        CreateRetireeCommand cmd = new CreateRetireeCommand();
        cmd.setFirstName("Anna");
        cmd.setLastName("Nowak");
        cmd.setGender(Gender.FEMALE);
        cmd.setPesel("69092961334");
        cmd.setEmail("anna@test.pl");
        cmd.setHeight(170.0);
        cmd.setWeight(60.0);
        cmd.setPassword("secret");
        cmd.setPensionAmount(new BigDecimal("1.84"));
        cmd.setYearsWorked(1);
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
    @Test
    void shouldThrowWhenAddingEntityWithNonNullId() {
        Employee e = new Employee();
        e.setId("test");
        e.setFirstName("Test");
        e.setLastName("User");
        e.setPesel("12345678901");
        e.setEmail("test@example.com");
        e.setHeight(180.0);
        e.setWeight(75.0);
        e.setGender(Gender.MALE);
        e.setPassword("secret");
        setTypeFromAnnotation(e);

        assertThatThrownBy(() -> employeeCrudService.add(e))
                .isInstanceOf(IllegalEntityStateException.class)
                .hasMessageContaining("Entity ID should be null before persisting!")
                .satisfies(ex -> {
                    IllegalEntityStateException ie = (IllegalEntityStateException) ex;
                    assertThat(ie.getEntityType()).isEqualTo(Employee.class);
                });
    }

    @Test
    void shouldThrowWhenNoDtoMapperFoundForClass() {
        NoAnnotationPerson person = new NoAnnotationPerson();
        assertThatThrownBy(() -> personDtoMapper.map(person))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No mapper found for class NoAnnotationPerson");
    }

    @Test
    void shouldThrowWhenNoDtoMapperFoundForApplyTypeSpecificData() {
        NoAnnotationPerson person = new NoAnnotationPerson();
        CommandWithoutAnnotation cmd = new CommandWithoutAnnotation();
        assertThatThrownBy(() -> personDtoMapper.applyTypeSpecificData(person, cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No mapper found for class NoAnnotationPerson");
    }
}
