package pl.kurs.test3roz.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pl.kurs.test3roz.views.PersonSummaryView;

public interface PersonSummaryViewRepository extends JpaRepository<PersonSummaryView, String>,
        JpaSpecificationExecutor<PersonSummaryView> {
}