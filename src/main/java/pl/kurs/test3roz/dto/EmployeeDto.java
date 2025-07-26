package pl.kurs.test3roz.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kurs.test3roz.models.PersonType;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@PersonType("EMPLOYEE")
@JsonTypeName("EMPLOYEE")
public class EmployeeDto extends PersonDto {
    private String currentPosition;
    private BigDecimal currentSalary;
}