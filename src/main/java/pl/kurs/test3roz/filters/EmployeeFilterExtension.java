package pl.kurs.test3roz.filters;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.models.PersonType;
import java.util.List;
import java.util.Map;

@Component
@PersonType("EMPLOYEE")
public class EmployeeFilterExtension extends AbstractFilterHelper implements PersonFilterExtension {

    @Override
    public void apply(Root<?> r, CriteriaBuilder cb, GetPersonFilter f, List<Predicate> p) {
        Map<String, Object> extra = f.getExtraFilters();
        if (extra == null) return;

        addDateFrom(extra, "employmentDateFrom", r, cb, p, "startDate");
        addDateTo(extra, "employmentDateTo", r, cb, p, "endDate");

        addBigDecimalRange(extra, "salaryFrom", "salaryTo", r, cb, p, "currentSalary");
        addIntegerRange(extra, "positionCountFrom", "positionCountTo", r, cb, p, "positionCount");
        addIntegerRange(extra, "professionCountFrom", "professionCountTo", r, cb, p, "professionCount");
    }
}


