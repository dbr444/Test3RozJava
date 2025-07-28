package pl.kurs.test3roz.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.test3roz.models.people.Person;

public interface PersonRepository extends JpaRepository<Person, String> {
}
