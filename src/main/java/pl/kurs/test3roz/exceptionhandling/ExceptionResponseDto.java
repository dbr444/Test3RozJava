package pl.kurs.test3roz.exceptionhandling;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.List;

@AllArgsConstructor
@Getter
public class ExceptionResponseDto {
    private List<? extends AbstractErrorDto> errors;
    private String responseStatus;
    private Timestamp date;
}
