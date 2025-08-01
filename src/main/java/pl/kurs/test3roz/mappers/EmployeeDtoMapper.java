package pl.kurs.test3roz.mappers;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.commands.CreateEmployeeCommand;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.dto.EmployeeDto;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.models.Position;
import pl.kurs.test3roz.models.people.Employee;
import pl.kurs.test3roz.models.people.Person;
import java.util.List;

@Component
@AllArgsConstructor
public class EmployeeDtoMapper implements IPersonDtoMapper<Employee> {

    private final ModelMapper modelMapper;

    @Override
    public PersonDto map(Employee employee) {
        EmployeeDto dto = modelMapper.map(employee, EmployeeDto.class);

        List<Position> positions = employee.getPositions();
        if (!positions.isEmpty()) {
            Position last = positions.get(positions.size() - 1);
            dto.setCurrentPosition(last.getJobName());
            dto.setCurrentSalary(last.getSalary());
        }

        return dto;
    }

    @Override
    public void applyTypeSpecificData(Person person, CreatePersonCommand command) {
        if (command instanceof CreateEmployeeCommand empCmd) {
            Position p = new Position();
            p.setJobName(empCmd.getCurrentPosition());
            p.setStartDate(empCmd.getHireDate());
            p.setEndDate(empCmd.getEndDate());
            p.setSalary(empCmd.getCurrentSalary());
            p.setEmployee((Employee) person);
            ((Employee) person).getPositions().add(p);
        }
    }

    @Override
    public Class<Employee> getSupportedClass() {
        return Employee.class;
    }
}
