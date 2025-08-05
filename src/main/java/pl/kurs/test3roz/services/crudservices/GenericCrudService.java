package pl.kurs.test3roz.services.crudservices;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.test3roz.exceptions.IllegalEntityIdException;
import pl.kurs.test3roz.exceptions.IllegalEntityStateException;
import pl.kurs.test3roz.exceptions.RequestedEntityNotFoundException;
import pl.kurs.test3roz.models.Identificationable;
import java.lang.reflect.ParameterizedType;

public abstract class GenericCrudService<T extends Identificationable, R extends JpaRepository<T, String>> implements ICrudService<T> {

    protected final R repository;
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

//    @Override // tutaj pokazuje tylko jakbym zamienił, ale korzystam nie korzystam z niej nigdzie, bo dedykowana jest inna w kontrolerze
//    @Transactional(readOnly = true)
//    public Page<T> getAll(Pageable pageable) {
//        return repository.findAll(pageable);
//    }

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