package pl.kurs.test3roz.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Subselect;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Subselect("SELECT * FROM person_management.position_view")
@Immutable
@Getter
@Setter
public class PositionView {
    @Id
    @Column(name = "id_position")
    private Long id;

    private String jobName;
    private BigDecimal salary;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long employeeId;
}
