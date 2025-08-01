package pl.kurs.test3roz.mappers;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.dto.StudentDto;
import pl.kurs.test3roz.models.people.Student;

@Component
@AllArgsConstructor
public class StudentDtoMapper implements IPersonDtoMapper<Student> {

    private final ModelMapper modelMapper;

    @Override
    public PersonDto map(Student student) {
        return modelMapper.map(student, StudentDto.class);
    }

    @Override
    public Class<Student> getSupportedClass() {
        return Student.class;
    }
}
