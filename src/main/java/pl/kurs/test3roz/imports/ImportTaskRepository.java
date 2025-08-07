package pl.kurs.test3roz.imports;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.test3roz.imports.models.ImportStatus;
import pl.kurs.test3roz.imports.models.Import;

import java.util.List;

public interface ImportTaskRepository extends JpaRepository<Import, String> {
    List<Import> findAllByStatusIn(List<ImportStatus> statuses);
}
