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
@PersonType("STUDENT")
@JsonTypeName("STUDENT")
public class StudentDto extends PersonDto {
    private String currentUniversityName;
    private Integer studyYear;
    private String studyMajor;
    private BigDecimal scholarshipAmount;
}
