package pl.kurs.test3roz.views;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Subselect;
import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Subselect("SELECT * FROM person_management.employee_summary_view")
@Immutable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class EmployeeSummaryView extends PersonSummaryView {

    private Long positionCount;

    private Long professionCount;

    private BigDecimal currentSalary;

    private String currentPosition;

    private LocalDate startDate;

    private LocalDate endDate;
}
