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
@Table(name = "retirees")
@PersonType("RETIREE")
public class Retiree extends Person {

    @Column(nullable = false)
    private BigDecimal pensionAmount;

    @Column(nullable = false)
    private int yearsWorked;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Retiree retiree = (Retiree) o;
        return Objects.equals(pesel, retiree.pesel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pesel);
    }

    @Override
    public String toString() {
        return "Retiree{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", pesel='" + pesel + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender +
                ", type='" + type + '\'' +
                ", pensionAmount=" + pensionAmount +
                ", yearsWorked=" + yearsWorked +
                '}';
    }
}