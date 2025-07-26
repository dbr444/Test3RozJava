package pl.kurs.test3roz.commands;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.models.Gender;
import pl.kurs.test3roz.models.people.Person;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@Getter
@Setter
public abstract class UpdatePersonCommand {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    @DecimalMin("0.0")
    private Double height;

    @NotNull
    @DecimalMin("0.0")
    private Double weight;

    @NotNull
    private Gender gender;

    @NotNull
    private Long version;

    @NotBlank
    private String password;

    public abstract Class<? extends Person> getTargetClass();
    public abstract Class<? extends PersonDto> getTargetDtoClass();
    public abstract void applyTo(Person person);
}
