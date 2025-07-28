package pl.kurs.test3roz.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.test3roz.models.Position;

public interface PositionRepository extends JpaRepository<Position, String> {
}