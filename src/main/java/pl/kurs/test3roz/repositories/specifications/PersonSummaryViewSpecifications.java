package pl.kurs.test3roz.repositories.specifications;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import pl.kurs.test3roz.filters.GetPersonFilter;
import pl.kurs.test3roz.models.Gender;
import pl.kurs.test3roz.views.PersonSummaryView;
import java.util.*;
import java.util.function.Consumer;

public class PersonSummaryViewSpecifications {

    public static Specification<PersonSummaryView> withFilter(GetPersonFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            addCommonFilters(f, root, cb, p);
            addStudentFilters(f, root, cb, p);
            addEmployeeFilters(f, root, cb, p);
            addRetireeFilters(f, root, cb, p);

            return cb.and(p.toArray(new Predicate[0]));
        };
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

    private static void addStudentFilters(GetPersonFilter f, Root<?> r, CriteriaBuilder cb, List<Predicate> p) {
        addIfNotBlank(f.getCurrentUniversityName(), v -> p.add(containsIgnoreCase(cb, r.get("currentUniversityName"), v)));
        addIfNotBlank(f.getStudyMajor(), v -> p.add(containsIgnoreCase(cb, r.get("studyMajor"), v)));
        addIfNotNull(f.getStudyYearFrom(), v -> p.add(cb.ge(r.get("studyYear"), v)));
        addIfNotNull(f.getStudyYearTo(), v -> p.add(cb.le(r.get("studyYear"), v)));
        addIfNotNull(f.getScholarshipAmountFrom(), v -> p.add(cb.ge(r.get("scholarshipAmount"), v)));
        addIfNotNull(f.getScholarshipAmountTo(), v -> p.add(cb.le(r.get("scholarshipAmount"), v)));
    }

    private static void addEmployeeFilters(GetPersonFilter f, Root<?> r, CriteriaBuilder cb, List<Predicate> p) {
        addIfNotNull(f.getEmploymentDateFrom(), v -> p.add(cb.greaterThanOrEqualTo(r.get("employmentDate"), v)));
        addIfNotNull(f.getEmploymentDateTo(), v -> p.add(cb.lessThanOrEqualTo(r.get("employmentDate"), v)));
        addIfNotNull(f.getPositionCountFrom(), v -> p.add(cb.ge(r.get("positionCount"), v)));
        addIfNotNull(f.getPositionCountTo(), v -> p.add(cb.le(r.get("positionCount"), v)));
        addIfNotNull(f.getProfessionCountFrom(), v -> p.add(cb.ge(r.get("professionCount"), v)));
        addIfNotNull(f.getProfessionCountTo(), v -> p.add(cb.le(r.get("professionCount"), v)));

        if (f.getSalaryFrom() != null || f.getSalaryTo() != null) {
            p.add(cb.equal(r.get("type"), "EMPLOYEE"));
            addIfNotNull(f.getSalaryFrom(), v -> p.add(cb.ge(r.get("currentSalary"), v)));
            addIfNotNull(f.getSalaryTo(), v -> p.add(cb.le(r.get("currentSalary"), v)));
        }
    }

    private static void addRetireeFilters(GetPersonFilter f, Root<?> r, CriteriaBuilder cb, List<Predicate> p) {
        if (f.getPensionAmountFrom() != null || f.getPensionAmountTo() != null) {
            p.add(cb.equal(r.get("type"), "RETIREE"));
            addIfNotNull(f.getPensionAmountFrom(), v -> p.add(cb.ge(r.get("pensionAmount"), v)));
            addIfNotNull(f.getPensionAmountTo(), v -> p.add(cb.le(r.get("pensionAmount"), v)));
        }

        addIfNotNull(f.getYearsWorkedFrom(), v -> p.add(cb.ge(r.get("yearsWorked"), v)));
        addIfNotNull(f.getYearsWorkedTo(), v -> p.add(cb.le(r.get("yearsWorked"), v)));
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
