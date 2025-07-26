package pl.kurs.test3roz.commands;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pl.kurs.test3roz.dto.EmployeeDto;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.models.PersonType;
import pl.kurs.test3roz.models.people.Employee;
import pl.kurs.test3roz.models.people.Person;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@PersonType("EMPLOYEE")
public class CreateEmployeeCommand extends CreatePersonCommand {

    @NotNull
    private LocalDate hireDate;

    @NotNull
    private LocalDate endDate;

    @NotBlank
    private String currentPosition;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal currentSalary;

    @Override
    public Class<? extends Person> getTargetClass() {
        return Employee.class;
    }

    @Override
    public Class<? extends PersonDto> getTargetDtoClass() {
        return EmployeeDto.class;
    }


}