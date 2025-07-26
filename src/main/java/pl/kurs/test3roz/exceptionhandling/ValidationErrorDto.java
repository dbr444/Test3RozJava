package pl.kurs.test3roz.exceptionhandling;

import lombok.Getter;

@Getter
public class ValidationErrorDto extends AbstractErrorDto {
    private String field;
    private Object rejectedValue;

    public ValidationErrorDto(String message, String field, Object rejectedValue) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
    }
}
