package pl.kurs.test3roz.models.people;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import pl.kurs.test3roz.models.Gender;
import pl.kurs.test3roz.models.Identificationable;
import java.io.Serializable;

@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "persons")
@Getter
@Setter
@Entity
public abstract class Person implements Identificationable, Serializable {
    static final long serialVersionUID = 42L;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")//znalazlem na stacku. zostawiam juz zeby nie popsuc bo bylo kombinowania:)
    @Column(name = "id_person", length = 36)
    private String id;

    @Version
    protected Long version;

    @Column(nullable = false)
    protected String firstName;

    @Column(nullable = false)
    protected String lastName;

    @Column(nullable = false, unique = true)
    protected String pesel;

    @Column(nullable = false)
    protected Double height;

    @Column(nullable = false)
    protected Double weight;

    @Column(nullable = false, unique = true)
    protected String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected Gender gender;

    @Column(nullable = false)
    protected String type;

    @JsonIgnore
    @Column(nullable = false)
    protected String password;
}