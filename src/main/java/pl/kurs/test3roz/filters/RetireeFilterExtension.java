package pl.kurs.test3roz.filters;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.models.PersonType;
import java.util.List;
import java.util.Map;

@Component
@PersonType("RETIREE")
public class RetireeFilterExtension extends AbstractFilterHelper implements PersonFilterExtension {

    @Override
    public void apply(Root<?> r, CriteriaBuilder cb, GetPersonFilter f, List<Predicate> p) {
        Map<String, Object> extra = f.getExtraFilters();
        if (extra == null) return;

        addBigDecimalRange(extra, "pensionAmountFrom", "pensionAmountTo", r, cb, p, "pensionAmount");
        addIntegerRange(extra, "yearsWorkedFrom", "yearsWorkedTo", r, cb, p, "yearsWorked");
    }
}
