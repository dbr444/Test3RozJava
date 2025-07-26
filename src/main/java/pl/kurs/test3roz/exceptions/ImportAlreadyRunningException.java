package pl.kurs.test3roz.exceptions;

public class ImportAlreadyRunningException extends RuntimeException {
    public ImportAlreadyRunningException(String message) {
        super(message);
    }
}