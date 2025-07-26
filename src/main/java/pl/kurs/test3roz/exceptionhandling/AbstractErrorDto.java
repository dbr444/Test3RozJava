package pl.kurs.test3roz.exceptionhandling;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public abstract class AbstractErrorDto {
    private String message;
}
