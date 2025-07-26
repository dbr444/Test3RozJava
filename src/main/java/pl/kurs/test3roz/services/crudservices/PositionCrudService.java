package pl.kurs.test3roz.services.crudservices;

import org.springframework.stereotype.Service;
import pl.kurs.test3roz.models.Position;
import pl.kurs.test3roz.repositories.PositionRepository;

@Service
public class PositionCrudService extends GenericCrudService<Position, PositionRepository> {
    public PositionCrudService(PositionRepository repository) {
        super(repository);
    }
}