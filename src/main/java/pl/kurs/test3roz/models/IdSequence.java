package pl.kurs.test3roz.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "id_sequence")
@Getter
@Setter
public class IdSequence {

    @Id
    @Column(name = "entity_name")
    private String name;

    @Column(name = "next_val")
    private Long nextVal;
}
