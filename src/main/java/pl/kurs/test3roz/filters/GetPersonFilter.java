package pl.kurs.test3roz.filters;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class GetPersonFilter {
    private String type;
    private String firstName;
    private String lastName;
    private String pesel;
    private String email;
    private String gender;
    private Double heightFrom;
    private Double heightTo;
    private Double weightFrom;
    private Double weightTo;
    private Map<String, Object> extraFilters;
}

