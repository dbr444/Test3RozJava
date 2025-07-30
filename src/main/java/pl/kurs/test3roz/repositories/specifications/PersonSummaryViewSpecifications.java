package pl.kurs.test3roz.repositories.specifications;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import pl.kurs.test3roz.filters.GetPersonFilter;
import pl.kurs.test3roz.filters.PersonFilterExtension;
import pl.kurs.test3roz.models.Gender;
import pl.kurs.test3roz.views.PersonSummaryView;
import java.util.*;
import java.util.function.Consumer;

public class PersonSummaryViewSpecifications {

    private static Map<String, PersonFilterExtension> extensions = Map.of();

    public static void setExtensions(Map<String, PersonFilterExtension> exts) {
        extensions = exts;
    }

    public static Specification<PersonSummaryView> withFilter(GetPersonFilter f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            addCommonFilters(f, root, cb, predicates);

            Map<String, Object> extra = f.getExtraFilters();
            String type = f.getType();

            if (extra != null && !extra.isEmpty()) {
                if (type != null) {
                    handleTypedFilter(f, root, cb, predicates, type);
                } else {
                    handleFlexibleFilter(f, root, cb, predicates);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void handleTypedFilter(GetPersonFilter f, Root<PersonSummaryView> root, CriteriaBuilder cb,
                                          List<Predicate> predicates, String type) {
        PersonFilterExtension ext = extensions.get(type.toUpperCase());
        if (ext == null)
            throw new IllegalStateException("Unknown person type: " + type);

        List<Predicate> tmp = new ArrayList<>();
        ext.apply(root, cb, f, tmp);
        if (tmp.isEmpty())
            throw new IllegalStateException("Provided extra filters can't be applide to type: " + type);

        predicates.add(cb.equal(root.get("type"), type.toUpperCase()));
        predicates.addAll(tmp);
    }

    private static void handleFlexibleFilter(GetPersonFilter f, Root<PersonSummaryView> root, CriteriaBuilder cb,
                                             List<Predicate> predicates) {
        extensions.forEach((typeKey, ext) -> {
            List<Predicate> tmp = new ArrayList<>();
            ext.apply(root, cb, f, tmp);
            if (!tmp.isEmpty()) {
                Predicate typePredicate = cb.equal(root.get("type"), typeKey);
                predicates.add(cb.and(typePredicate, cb.and(tmp.toArray(new Predicate[0]))));
            }
        });
    }


    private static void addCommonFilters(GetPersonFilter f, Root<?> r, CriteriaBuilder cb, List<Predicate> p) {
        addIfNotBlank(f.getFirstName(), v -> p.add(containsIgnoreCase(cb, r.get("firstName"), v)));
        addIfNotBlank(f.getLastName(), v -> p.add(containsIgnoreCase(cb, r.get("lastName"), v)));
        addIfNotBlank(f.getEmail(), v -> p.add(containsIgnoreCase(cb, r.get("email"), v)));
        addIfNotNull(f.getPesel(), v -> p.add(cb.equal(r.get("pesel"), v)));
        addIfNotNull(f.getType(), v -> p.add(cb.equal(r.get("type"), v.toUpperCase())));

        addIfNotBlank(f.getGender(), v -> {
            try {
                p.add(cb.equal(r.get("gender"), Gender.valueOf(v.toUpperCase())));
            } catch (IllegalArgumentException ignored) {}
        });

        addIfNotNull(f.getHeightFrom(), v -> p.add(cb.ge(r.get("height"), v)));
        addIfNotNull(f.getHeightTo(), v -> p.add(cb.le(r.get("height"), v)));
        addIfNotNull(f.getWeightFrom(), v -> p.add(cb.ge(r.get("weight"), v)));
        addIfNotNull(f.getWeightTo(), v -> p.add(cb.le(r.get("weight"), v)));
    }

    private static Predicate containsIgnoreCase(CriteriaBuilder cb, Path<String> path, String value) {
        return cb.like(cb.lower(path), "%" + value.toLowerCase() + "%");
    }

    private static <T> void addIfNotNull(T value, Consumer<T> consumer) {
        if (value != null) consumer.accept(value);
    }

    private static void addIfNotBlank(String value, Consumer<String> consumer) {
        if (value != null && !value.trim().isEmpty()) consumer.accept(value);
    }
}
