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
@PersonType("RETIREE")
@JsonTypeName("RETIREE")
public class RetireeDto extends PersonDto {
    private BigDecimal pensionAmount;
    private int yearsWorked;
}