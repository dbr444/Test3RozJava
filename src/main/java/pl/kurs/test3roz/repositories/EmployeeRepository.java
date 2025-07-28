package pl.kurs.test3roz.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kurs.test3roz.models.people.Employee;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Employee e left join fetch e.positions where e.id = :id")
    Optional<Employee> findByIdWithLock(@Param("id") String id);
}