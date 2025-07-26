CREATE SCHEMA IF NOT EXISTS person_management;

CREATE VIEW person_management.person_summary_view AS
SELECT
    1 AS id_person,
    'janek.kowal@test.pl' AS email,
    'Janek' AS first_name,
    'Kowal' AS last_name,
    'MALE' AS gender,
    185.0 AS height,
    '12345678901' AS pesel,
    'EMPLOYEE' AS type,
    0 AS version,
    85.0 AS weight;

CREATE VIEW person_management.employee_summary_view AS
SELECT
    1 AS id_person,
    'Tester' AS current_position,
    9876.54 AS current_salary,
    CURRENT_DATE AS end_date,
    1 AS position_count,
    2 AS profession_count,
    CURRENT_DATE AS start_date;

CREATE VIEW person_management.retiree_summary_view AS
SELECT 1 AS id_person, 2500 AS pension_amount, 35 AS years_worked;

CREATE VIEW person_management.student_summary_view AS
SELECT
    1 AS id_person,
    'Politechnika' AS current_university_name,
    1500 AS scholarship_amount,
    'Informatyka' AS study_major,
    2 AS study_year;
