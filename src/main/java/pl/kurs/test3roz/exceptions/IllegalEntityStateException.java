package pl.kurs.test3roz.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalEntityStateException extends IllegalStateException {
    private Class<?> entityType;

    public IllegalEntityStateException(String message, Class<?> entityType) {
        super(message);
        this.entityType = entityType;
    }
}
