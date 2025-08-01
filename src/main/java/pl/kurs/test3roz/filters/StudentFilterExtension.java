package pl.kurs.test3roz.filters;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.models.PersonType;
import java.util.List;
import java.util.Map;

@Component
@PersonType("STUDENT")
public class StudentFilterExtension extends AbstractFilterHelper implements PersonFilterExtension {

    @Override
    public void apply(Root<?> r, CriteriaBuilder cb, GetPersonFilter f, List<Predicate> p) {
        Map<String, Object> extra = f.getExtraFilters();
        if (extra == null) return;

        addTextContains(extra, "studyMajor", r, cb, p, "studyMajor");
        addIntegerRange(extra, "studyYearFrom", "studyYearTo", r, cb, p, "studyYear");
        addBigDecimalRange(extra, "scholarshipAmountFrom", "scholarshipAmountTo", r, cb, p, "scholarshipAmount");
        addTextContains(extra, "currentUniversityName", r, cb, p, "currentUniversityName");
    }
}
