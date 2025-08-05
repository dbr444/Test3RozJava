package pl.kurs.test3roz.services.crudservices;

public interface ICrudService<T> {
    T add(T entity);
    T get(String id);
    void deleteAllEntities();
}