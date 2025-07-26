package pl.kurs.test3roz.filters;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class GetPersonFilter {

    // all
    private String type;
    private String firstName;
    private String lastName;
    private String pesel;
    private String email;
    private String gender;

    private Double heightFrom;
    private Double heightTo;
    private Double weightFrom;
    private Double weightTo;

    // Student
    private String currentUniversityName;
    private String studyMajor;
    private Integer studyYearFrom;
    private Integer studyYearTo;
    private BigDecimal scholarshipAmountFrom;
    private BigDecimal scholarshipAmountTo;

    // Employee
    private LocalDate employmentDateFrom;
    private LocalDate employmentDateTo;
    private BigDecimal salaryFrom;
    private BigDecimal salaryTo;
    private Integer positionCountFrom;
    private Integer positionCountTo;
    private Integer professionCountFrom;
    private Integer professionCountTo;

    // Retiree
    private BigDecimal pensionAmountFrom;
    private BigDecimal pensionAmountTo;
    private Integer yearsWorkedFrom;
    private Integer yearsWorkedTo;
}
