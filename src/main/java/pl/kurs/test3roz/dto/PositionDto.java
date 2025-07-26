package pl.kurs.test3roz.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@JsonTypeName("POSITION")
public class PositionDto {
    private Long id;
    private String jobName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal salary;
    private Long employeeId;
}