package pl.kurs.test3roz.controllers;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.test3roz.models.Gender;
import pl.kurs.test3roz.models.IdGenerator;
import pl.kurs.test3roz.models.Position;
import pl.kurs.test3roz.models.people.Employee;
import pl.kurs.test3roz.services.crudservices.EmployeeCrudService;
import pl.kurs.test3roz.services.crudservices.PersonCrudService;
import pl.kurs.test3roz.services.crudservices.PositionCrudService;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class PositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonCrudService personCrudService;

    @Autowired
    private EmployeeCrudService employeeCrudService;

    @Autowired
    private PositionCrudService positionCrudService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private IdGenerator idGenerator;

    private Employee employee;

    @BeforeEach
    void setUp() {
        positionCrudService.deleteAllEntities();
        personCrudService.deleteAllEntities();

        idGenerator.getEm()
                .createNativeQuery("INSERT INTO id_sequence (entity_name, next_val) VALUES ('person', 1)")
                .executeUpdate();

        employee = new Employee();
        employee.setFirstName("Anna");
        employee.setLastName("Nowak");
        employee.setPesel("01234567890");
        employee.setEmail("anna.nowak@test.pl");
        employee.setHeight(165.0);
        employee.setWeight(55.0);
        employee.setGender(Gender.FEMALE);
        employee.setPassword("pass");
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
    void shouldAssignPositionToEmployeeSuccessfully() throws Exception {
        String body = Files.readString(Path.of("src/test/resources/json/assign-position.json"));

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(user("testuser").roles("EMPLOYEE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value(employee.getId()))
                .andExpect(jsonPath("$.jobName").value("Tester"))
                .andExpect(jsonPath("$.salary").value(9876.54));

        Employee updated = employeeCrudService.findByIdWithLock(employee.getId());
        assertThat(updated.getPositions()).hasSize(1);

        Position assigned = updated.getPositions().get(0);
        assertThat(assigned.getJobName()).isEqualTo("Tester");
        assertThat(assigned.getSalary()).isEqualByComparingTo(BigDecimal.valueOf(9876.54));
        assertThat(assigned.getStartDate()).isEqualTo(LocalDate.of(2055, 8, 1));
        assertThat(assigned.getEndDate()).isEqualTo(LocalDate.of(2057, 12, 31));
    }

    @Test
    void shouldThrowWhenEndDateIsBeforeStartDate() throws Exception {
        String body = Files.readString(Path.of("src/test/resources/json/assign-position-invalid-dates.json"));

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(user("testuser").roles("EMPLOYEE")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("End date must not be before start date")));
    }

    @Test
    void shouldThrowWhenDatesOverlapWithExistingPosition() throws Exception {
        String first = Files.readString(Path.of("src/test/resources/json/assign-position.json"));
        mockMvc.perform(post("/api/employees/" + employee.getId() + "/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(first)
                        .with(user("testuser").roles("EMPLOYEE")))
                .andExpect(status().isCreated());

        String overlapping = Files.readString(Path.of("src/test/resources/json/assign-position.json"));

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(overlapping)
                        .with(user("testuser").roles("EMPLOYEE")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Daty nakładają się na istniejące stanowisko.")));
    }
}
