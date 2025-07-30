package pl.kurs.test3roz.filters;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;

public interface PersonFilterExtension {
    void apply(Root<?> r, CriteriaBuilder cb, GetPersonFilter f, List<Predicate> p);
}
