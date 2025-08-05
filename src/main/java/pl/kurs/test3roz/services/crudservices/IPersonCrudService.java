package pl.kurs.test3roz.services.crudservices;

import pl.kurs.test3roz.models.people.Person;

public interface IPersonCrudService<T extends Person> extends ICrudService<T> {
    Class<T> getSupportedClass();

    default T getWithRelations(String id) {
        return get(id);
    }
}
