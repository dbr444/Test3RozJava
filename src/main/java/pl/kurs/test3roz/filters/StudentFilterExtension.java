package pl.kurs.test3roz.filters;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.models.PersonType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@PersonType("STUDENT")
public class StudentFilterExtension implements PersonFilterExtension {

    @Override
    public void apply(Root<?> r, CriteriaBuilder cb, GetPersonFilter f, List<Predicate> p) {
        Map<String, Object> extra = f.getExtraFilters();
        if (extra == null) return;

        if (extra.get("studyMajor") instanceof String v && !v.isBlank())
            p.add(cb.like(cb.lower(r.get("studyMajor")), "%" + v.toLowerCase() + "%"));

        if (extra.get("studyYearFrom") instanceof Integer v)
            p.add(cb.ge(r.get("studyYear"), v));

        if (extra.get("studyYearTo") instanceof Integer v)
            p.add(cb.le(r.get("studyYear"), v));

        if (extra.get("scholarshipAmountFrom") instanceof Number v)
            p.add(cb.ge(r.get("scholarshipAmount"), new BigDecimal(v.toString())));

        if (extra.get("scholarshipAmountTo") instanceof Number v)
            p.add(cb.le(r.get("scholarshipAmount"), new BigDecimal(v.toString())));

        if (extra.get("currentUniversityName") instanceof String v && !v.isBlank())
            p.add(cb.like(cb.lower(r.get("currentUniversityName")), "%" + v.toLowerCase() + "%"));

    }
}