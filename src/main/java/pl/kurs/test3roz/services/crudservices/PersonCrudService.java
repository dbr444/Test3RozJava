package pl.kurs.test3roz.services.crudservices;

import org.springframework.stereotype.Service;
import pl.kurs.test3roz.models.people.Person;
import pl.kurs.test3roz.repositories.PersonRepository;

@Service
public class PersonCrudService extends GenericCrudService<Person, PersonRepository> {
    public PersonCrudService(PersonRepository repository) {
        super(repository);
    }
}