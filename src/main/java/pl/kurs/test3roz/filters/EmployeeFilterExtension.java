package pl.kurs.test3roz.filters;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.models.PersonType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@PersonType("EMPLOYEE")
public class EmployeeFilterExtension implements PersonFilterExtension {

    @Override
    public void apply(Root<?> r, CriteriaBuilder cb, GetPersonFilter f, List<Predicate> p) {
        Map<String, Object> extra = f.getExtraFilters();
        if (extra == null) return;

        if (extra.get("employmentDateFrom") instanceof String v)
            p.add(cb.greaterThanOrEqualTo(r.get("startDate"), LocalDate.parse(v)));

        if (extra.get("employmentDateTo") instanceof String v)
            p.add(cb.lessThanOrEqualTo(r.get("endDate"), LocalDate.parse(v)));

        if (extra.get("salaryFrom") instanceof Number v)
            p.add(cb.ge(r.get("currentSalary"), new BigDecimal(v.toString())));

        if (extra.get("salaryTo") instanceof Number v)
            p.add(cb.le(r.get("currentSalary"), new BigDecimal(v.toString())));

        if (extra.get("positionCountFrom") instanceof Number v)
            p.add(cb.ge(r.get("positionCount"), v.intValue()));

        if (extra.get("positionCountTo") instanceof Number v)
            p.add(cb.le(r.get("positionCount"), v.intValue()));

        if (extra.get("professionCountFrom") instanceof Number v)
            p.add(cb.ge(r.get("professionCount"), v.intValue()));

        if (extra.get("professionCountTo") instanceof Number v)
            p.add(cb.le(r.get("professionCount"), v.intValue()));
    }
}
