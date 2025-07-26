package pl.kurs.test3roz.models.people;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kurs.test3roz.models.PersonType;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "students")
@PersonType("STUDENT")
public class Student extends Person {

    @Column(nullable = false)
    private String currentUniversityName;

    @Column(nullable = false)
    private int studyYear;

    @Column(nullable = false)
    private String studyMajor;

    @Column(nullable = false)
    private BigDecimal scholarshipAmount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(pesel, student.pesel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pesel);
    }

    @Override
    public String toString() {
        return "Student{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", pesel='" + pesel + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender +
                ", type='" + type + '\'' +
                ", currentUniversityName='" + currentUniversityName + '\'' +
                ", studyYear=" + studyYear +
                ", studyMajor='" + studyMajor + '\'' +
                ", scholarshipAmount=" + scholarshipAmount +
                '}';
    }
}
