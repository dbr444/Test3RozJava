package pl.kurs.test3roz.exceptions;

import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RequestedEntityNotFoundException extends EntityNotFoundException {
    private Class<?> entityType;

    public RequestedEntityNotFoundException(String message, Class<?> entityType) {
        super(message);
        this.entityType = entityType;
    }
}
