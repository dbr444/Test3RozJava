package pl.kurs.test3roz.views;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Subselect;
import pl.kurs.test3roz.models.Gender;
import pl.kurs.test3roz.models.Identificationable;
import javax.annotation.concurrent.Immutable;

@Entity
@Subselect("SELECT * FROM person_management.person_summary_view")
@Immutable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class PersonSummaryView implements Identificationable {

    @Id
    @Column(name = "id_person")
    private String id;

    @Version
    private Long version;

    private String firstName;

    private String lastName;

    private String pesel;

    private Double height;

    private Double weight;

    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String type;
}
