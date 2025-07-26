package pl.kurs.test3roz.views;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Subselect;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;

@Entity
@Subselect("SELECT * FROM person_management.student_summary_view")
@Immutable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryView extends PersonSummaryView {

    private String currentUniversityName;

    private Integer studyYear;

    private String studyMajor;

    private BigDecimal scholarshipAmount;
}
