package pl.kurs.test3roz.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateOrderValidator.class)
public @interface ValidDateOrder {
    String message() default "End date must not be before start date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
