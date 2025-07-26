package pl.kurs.test3roz.exceptionhandling;

import lombok.Getter;

@Getter
public class ErrorDto extends AbstractErrorDto {

    private String errorType;
    private Class<?> entityType;

    public ErrorDto(String message, String errorType) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorDto(String message, String errorType, Class<?> entityType) {
        super(message);
        this.errorType = errorType;
        this.entityType = entityType;
    }
}
