package pl.kurs.test3roz.services.crudservices;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import pl.kurs.test3roz.exceptions.IllegalEntityIdException;
import pl.kurs.test3roz.exceptions.IllegalEntityStateException;
import pl.kurs.test3roz.exceptions.RequestedEntityNotFoundException;
import pl.kurs.test3roz.models.Identificationable;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public abstract class GenericCrudService<T extends Identificationable, R extends JpaRepository<T, Long>> implements ICrudService<T> {

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

    @Transactional
    public T addWithManualId(T entity) {
        if (entity.getId() == null) {
            throw new IllegalEntityStateException("Entity ID must be set before persisting!", entityType);
        }
        return repository.save(entity);
    }


    @Transactional
    public List<T> addAllWithManualId(List<T> entities) {
        for (T e : entities) {
            if (e.getId() == null) {
                throw new IllegalEntityStateException("Entity ID must be set before persisting!", entityType);
            }
        }
        return repository.saveAll(entities);
    }

//
//    @Override
//    @Transactional
//    public T update(T entity) {
//        if (entity.getId() == null) {
//            throw new IllegalEntityIdException("Id shouldn't be null!", entityType);
//        }
//
//        try {
//            T existing = entityManager.find(entityType, entity.getId(), LockModeType.OPTIMISTIC);
//            if (existing == null) {
//                throw new RequestedEntityNotFoundException("Entity with id " + entity.getId() + " not found!", entityType);
//            }
//
//            entityManager.detach(existing);
//            return repository.save(entity);
//
//        } catch (OptimisticLockException e) {
//            throw new ObjectOptimisticLockingFailureException(entityType, entity.getId());
//        }
//    }

    @Override
    @Transactional
    public List<T> getAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public T get(Long id) {
        if (id == null) {
            throw new IllegalEntityIdException("Id shouldn't be null!", entityType);
        }
        return repository.findById(id)
                .orElseThrow(() -> new RequestedEntityNotFoundException("Entity with id " + id + " not found!", entityType));
    }

    @Override//stworzona celowo na potrzeby projektu, zeby nie tworzyc juz oddzielnego entitymanagera do testów i żeby mniej było kodu:)
    public void deleteAllEntities() {
        repository.deleteAllInBatch();  // ✅ bez fetchowania relacji z bazy
    }

}