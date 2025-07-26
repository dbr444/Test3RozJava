package pl.kurs.test3roz.exceptionhandling;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.kurs.test3roz.exceptions.IllegalEntityIdException;
import pl.kurs.test3roz.exceptions.IllegalEntityStateException;
import pl.kurs.test3roz.exceptions.ImportAlreadyRunningException;
import pl.kurs.test3roz.exceptions.RequestedEntityNotFoundException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RequestedEntityNotFoundException.class)
    public ResponseEntity<ExceptionResponseDto> handleRequestedEntityNotFoundException(RequestedEntityNotFoundException e){
        ExceptionResponseDto exceptionResponseDto = new ExceptionResponseDto(
                List.of(new ErrorDto(e.getMessage(), e.getClass().getSimpleName(), e.getEntityType())),
                "NOT_FOUND",
                Timestamp.from(Instant.now())
        );
        return new ResponseEntity<>(exceptionResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalEntityIdException.class)
    public ResponseEntity<ExceptionResponseDto> handleIllegalEntityIdException(IllegalEntityIdException e) {
        ExceptionResponseDto exceptionResponseDto = new ExceptionResponseDto(
                List.of(new ErrorDto(e.getMessage(), e.getClass().getSimpleName(), e.getEntityType())),
                "BAD_REQUEST",
                Timestamp.from(Instant.now())
        );
        return new ResponseEntity<>(exceptionResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalEntityStateException.class)
    public ResponseEntity<ExceptionResponseDto> handleIIllegalEntityStateException(IllegalEntityStateException e) {
        ExceptionResponseDto exceptionResponseDto = new ExceptionResponseDto(
                List.of(new ErrorDto(e.getMessage(), e.getClass().getSimpleName(), e.getEntityType())),
                "BAD_REQUEST",
                Timestamp.from(Instant.now())
        );
        return new ResponseEntity<>(exceptionResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponseDto> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        ExceptionResponseDto exceptionResponseDto = new ExceptionResponseDto(
                List.of(new ErrorDto(e.getMessage(), e.getClass().getSimpleName())),
                "BAD_REQUEST",
                Timestamp.from(Instant.now())
        );
        return new ResponseEntity<>(exceptionResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleIllegalEntityIdException(MethodArgumentNotValidException e) {

        List<AbstractErrorDto> abstractErrorDtoList = new ArrayList<>();
        e.getBindingResult().getAllErrors()
                .forEach(err -> {
                    if (err instanceof FieldError) {
                        abstractErrorDtoList.add(new ValidationErrorDto(err.getDefaultMessage(), ((FieldError) err).getField(), ((FieldError) err).getRejectedValue()));
                    } else {
                        abstractErrorDtoList.add(new ErrorDto(err.getDefaultMessage(), err.getObjectName()));
                    }
                });

        ExceptionResponseDto exceptionResponseDto = new ExceptionResponseDto(
                abstractErrorDtoList,
                "BAD_REQUEST",
                Timestamp.from(Instant.now())
        );
        return new ResponseEntity<>(exceptionResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<String> handleOptimisticLock(OptimisticLockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Data has been modified concurrently. Please refresh and try again.");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ExceptionResponseDto> handleIllegalStateException(IllegalStateException e) {
        ExceptionResponseDto response = new ExceptionResponseDto(
                List.of(new ErrorDto(e.getMessage(), e.getClass().getSimpleName())),
                "BAD_REQUEST",
                Timestamp.from(Instant.now())
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponseDto> handleConstraintViolationException(ConstraintViolationException e) {
        List<AbstractErrorDto> errors = e.getConstraintViolations().stream()
                .map(cv -> new ValidationErrorDto(
                        cv.getMessage(),
                        cv.getPropertyPath().toString(),
                        cv.getInvalidValue()
                ))
                .collect(Collectors.toList());


        ExceptionResponseDto response = new ExceptionResponseDto(
                errors,
                "BAD_REQUEST",
                Timestamp.from(Instant.now())
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ImportAlreadyRunningException.class)
    public ResponseEntity<String> handleImportAlreadyRunning(ImportAlreadyRunningException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}




