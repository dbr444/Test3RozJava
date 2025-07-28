package pl.kurs.test3roz.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ImportParseException extends RuntimeException {
    public ImportParseException(String message, Throwable cause) {
        super(message, cause);
    }
}