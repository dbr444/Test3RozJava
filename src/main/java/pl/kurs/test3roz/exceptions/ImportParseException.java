package pl.kurs.test3roz.exceptions;

public class ImportParseException extends RuntimeException {
    public ImportParseException(String message, Throwable cause) {
        super(message, cause);
    }
}