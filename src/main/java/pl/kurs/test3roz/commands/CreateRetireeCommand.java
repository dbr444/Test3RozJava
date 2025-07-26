package pl.kurs.test3roz.commands;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.dto.RetireeDto;
import pl.kurs.test3roz.models.PersonType;
import pl.kurs.test3roz.models.people.Person;
import pl.kurs.test3roz.models.people.Retiree;
import java.math.BigDecimal;

@Getter
@Setter
@PersonType("RETIREE")
public class CreateRetireeCommand extends CreatePersonCommand {

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal pensionAmount;

    @Min(0)
    private int yearsWorked;

    @Override
    public Class<? extends Person> getTargetClass() {
        return Retiree.class;
    }

    @Override
    public Class<? extends PersonDto> getTargetDtoClass() {
        return RetireeDto.class;
    }
}