package pl.kurs.test3roz.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.kurs.test3roz.commands.CreatePositionCommand;

public class DateOrderValidator implements ConstraintValidator<ValidDateOrder, CreatePositionCommand> {

    @Override
    public boolean isValid(CreatePositionCommand command, ConstraintValidatorContext context) {
        return !command.getEndDate().isBefore(command.getStartDate());
    }
}
