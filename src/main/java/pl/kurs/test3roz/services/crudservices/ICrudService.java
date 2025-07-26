package pl.kurs.test3roz.services.crudservices;

import java.util.List;

public interface ICrudService<T> {
    T add(T entity);
//    T update(T entity);
    T get(Long id);
    List<T> getAll();
    void deleteAllEntities();
}