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
@PersonType("RETIREE")
public class RetireeFilterExtension implements PersonFilterExtension {

    @Override
    public void apply(Root<?> r, CriteriaBuilder cb, GetPersonFilter f, List<Predicate> p) {
        Map<String, Object> extra = f.getExtraFilters();
        if (extra == null) return;

        if (extra.get("pensionAmountFrom") instanceof Number v)
            p.add(cb.ge(r.get("pensionAmount"), new BigDecimal(v.toString())));

        if (extra.get("pensionAmountTo") instanceof Number v)
            p.add(cb.le(r.get("pensionAmount"), new BigDecimal(v.toString())));

        if (extra.get("yearsWorkedFrom") instanceof Number v)
            p.add(cb.ge(r.get("yearsWorked"), v.intValue()));

        if (extra.get("yearsWorkedTo") instanceof Number v)
            p.add(cb.le(r.get("yearsWorked"), v.intValue()));
    }
}
