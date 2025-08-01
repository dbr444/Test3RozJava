package pl.kurs.test3roz.filters;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public abstract class AbstractFilterHelper {

    protected void addBigDecimalRange(Map<String, Object> map, String fromKey, String toKey,
                                      Root<?> r, CriteriaBuilder cb, List<Predicate> p, String field) {
        if (map.get(fromKey) instanceof Number v)
            p.add(cb.ge(r.get(field), new BigDecimal(v.toString())));
        if (map.get(toKey) instanceof Number v)
            p.add(cb.le(r.get(field), new BigDecimal(v.toString())));
    }

    protected void addIntegerRange(Map<String, Object> map, String fromKey, String toKey,
                                   Root<?> r, CriteriaBuilder cb, List<Predicate> p, String field) {
        if (map.get(fromKey) instanceof Number v)
            p.add(cb.ge(r.get(field), v.intValue()));
        if (map.get(toKey) instanceof Number v)
            p.add(cb.le(r.get(field), v.intValue()));
    }

    protected void addTextContains(Map<String, Object> map, String key,
                                   Root<?> r, CriteriaBuilder cb, List<Predicate> p, String field) {
        if (map.get(key) instanceof String v && !v.isBlank())
            p.add(cb.like(cb.lower(r.get(field)), "%" + v.toLowerCase() + "%"));
    }

    protected void addDateFrom(Map<String, Object> map, String key, Root<?> r,
                               CriteriaBuilder cb, List<Predicate> p, String field) {
        if (map.get(key) instanceof String v)
            p.add(cb.greaterThanOrEqualTo(r.get(field), LocalDate.parse(v)));
    }

    protected void addDateTo(Map<String, Object> map, String key, Root<?> r,
                             CriteriaBuilder cb, List<Predicate> p, String field) {
        if (map.get(key) instanceof String v)
            p.add(cb.lessThanOrEqualTo(r.get(field), LocalDate.parse(v)));
    }
}
