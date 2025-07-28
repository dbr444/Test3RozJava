package pl.kurs.test3roz.services.crudservices;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.test3roz.exceptions.IllegalEntityIdException;
import pl.kurs.test3roz.exceptions.IllegalEntityStateException;
import pl.kurs.test3roz.exceptions.RequestedEntityNotFoundException;
import pl.kurs.test3roz.models.Identificationable;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public abstract class GenericCrudService<T extends Identificationable, R extends JpaRepository<T, String>> implements ICrudService<T> {

    protected final R repository;
    @Autowired
    protected EntityManager entityManager;
    protected final Class<T> entityType;


    public GenericCrudService(R repository) {
        this.repository = repository;
        this.entityType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }


    @Override
    @Transactional
    public T add(T entity) {
        if (entity.getId() != null) {
            throw new IllegalEntityStateException("Entity ID should be null before persisting!", entityType);
        }
        repository.save(entity);
        return entity;
    }

    @Override
    @Transactional
    public List<T> getAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public T get(String id) {
        if (id == null) {
            throw new IllegalEntityIdException("Id shouldn't be null!", entityType);
        }
        return repository.findById(id)
                .orElseThrow(() -> new RequestedEntityNotFoundException("Entity with id " + id + " not found!", entityType));
    }

    @Override//stworzona celowo na potrzeby projektu, zeby nie tworzyc juz oddzielnego entitymanagera do testów i żeby mniej było kodu:)
    public void deleteAllEntities() {
        repository.deleteAllInBatch();
    }

}