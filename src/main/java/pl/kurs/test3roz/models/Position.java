package pl.kurs.test3roz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import pl.kurs.test3roz.models.people.Employee;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "positions", uniqueConstraints = {@UniqueConstraint(columnNames = {"employee_id", "start_date"})})
public class Position implements Identificationable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id_position", length = 36)
    private String id;

    @Column(nullable = false)
    private String jobName;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private BigDecimal salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return Objects.equals(jobName, position.jobName) && Objects.equals(startDate, position.startDate) && Objects.equals(endDate, position.endDate) && Objects.equals(salary, position.salary) && Objects.equals(employee, position.employee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, startDate, endDate, salary, employee);
    }

    @Override
    public String toString() {
        return "Position{" +
                "id=" + id +
                ", jobName='" + jobName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", salary=" + salary +
                '}';
    }
}