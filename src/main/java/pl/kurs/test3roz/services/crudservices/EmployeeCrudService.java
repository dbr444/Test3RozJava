package pl.kurs.test3roz.services.crudservices;

import org.springframework.stereotype.Service;
import pl.kurs.test3roz.exceptions.RequestedEntityNotFoundException;
import pl.kurs.test3roz.models.people.Employee;
import pl.kurs.test3roz.repositories.EmployeeRepository;

@Service
public class EmployeeCrudService extends GenericCrudService<Employee, EmployeeRepository>
        implements IPersonCrudService<Employee> {
    public EmployeeCrudService(EmployeeRepository repository) {
        super(repository);
    }

    public Employee findByIdWithLock(String id) {
        return repository.findByIdWithLock(id)
                .orElseThrow(() -> new RequestedEntityNotFoundException("Entity not found!", entityType));
    }

    @Override
    public Employee getWithRelations(String id) {
        return repository.findByIdWithPositions(id)
                .orElseThrow(() -> new RequestedEntityNotFoundException("Entity not found!", entityType));
    }

    @Override
    public Class<Employee> getSupportedClass() {
        return Employee.class;
    }
}