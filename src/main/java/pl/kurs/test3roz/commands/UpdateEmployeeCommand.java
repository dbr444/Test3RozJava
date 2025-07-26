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

@Getter
@Setter
@PersonType("EMPLOYEE")
public class UpdateEmployeeCommand extends UpdatePersonCommand {
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

    @Override
    public void applyTo(Person person) {
        if (!(person instanceof Employee)) {
            throw new IllegalArgumentException("Expected Employee, got " + person.getClass().getSimpleName());
        }
    }
}