package pl.kurs.test3roz.services;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.kurs.test3roz.commands.CreatePositionCommand;
import pl.kurs.test3roz.dto.PositionDto;
import pl.kurs.test3roz.models.Position;
import pl.kurs.test3roz.models.people.Employee;
import pl.kurs.test3roz.services.crudservices.EmployeeCrudService;
import pl.kurs.test3roz.services.crudservices.PositionCrudService;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PositionService {

    private final PositionCrudService positionCrudService;
    private final EmployeeCrudService employeeCrudService;
    private final ModelMapper modelMapper;

    public PositionService(PositionCrudService positionCrudService, EmployeeCrudService employeeCrudService, ModelMapper modelMapper) {
        this.positionCrudService = positionCrudService;
        this.employeeCrudService = employeeCrudService;
        this.modelMapper = modelMapper;
    }

    public PositionDto assignPositionToEmployee(String employeeId, CreatePositionCommand command) {
        Employee employee = employeeCrudService.findByIdWithLock(employeeId);
        validateDates(command.getStartDate(), command.getEndDate(), employee.getPositions());

        Position position = mapToPosition(command, employee);
        Position saved = positionCrudService.add(position);

        PositionDto dto = modelMapper.map(saved, PositionDto.class);
        dto.setEmployeeId(saved.getEmployee().getId());
        return dto;
    }

    //helpers
    private Position mapToPosition(CreatePositionCommand c, Employee e) {
        Position p = new Position();
        p.setJobName(c.getJobName());
        p.setStartDate(c.getStartDate());
        p.setEndDate(c.getEndDate());
        p.setSalary(c.getSalary());
        p.setEmployee(e);
        e.getPositions().add(p);
        return p;
    }

    private void validateDates(LocalDate newStart, LocalDate newEnd, List<Position> existing) {
        validateDateOrder(newStart, newEnd);
        ensureNoOverlapping(existing, newStart, newEnd);
    }

    private void validateDateOrder(LocalDate newStart, LocalDate newEnd) {
        if (newEnd != null && newEnd.isBefore(newStart))
            throw new IllegalStateException("End date must not be before start date");
    }

    private void ensureNoOverlapping(List<Position> existing, LocalDate newStart, LocalDate newEnd) {
        boolean overlaps = existing.stream().anyMatch(pos ->
                !(newEnd.isBefore(pos.getStartDate()) || newStart.isAfter(pos.getEndDate()))
        );

        if (overlaps)
            throw new IllegalStateException("Daty nakładają się na istniejące stanowisko.");
    }

}
