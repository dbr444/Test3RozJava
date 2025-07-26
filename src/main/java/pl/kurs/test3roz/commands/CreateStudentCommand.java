package pl.kurs.test3roz.commands;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.dto.StudentDto;
import pl.kurs.test3roz.models.PersonType;
import pl.kurs.test3roz.models.people.Person;
import pl.kurs.test3roz.models.people.Student;
import java.math.BigDecimal;

@Getter
@Setter
@PersonType("STUDENT")
public class CreateStudentCommand extends CreatePersonCommand {

    @NotBlank
    private String currentUniversityName;

    @Min(1)
    private int studyYear;

    @NotBlank
    private String studyMajor;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal scholarshipAmount;

    @Override
    public Class<? extends Person> getTargetClass() {
        return Student.class;
    }

    @Override
    public Class<? extends PersonDto> getTargetDtoClass() {
        return StudentDto.class;
    }
}
