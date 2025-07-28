package pl.kurs.test3roz.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kurs.test3roz.commands.CreatePositionCommand;
import pl.kurs.test3roz.dto.PositionDto;
import pl.kurs.test3roz.services.PositionService;

@RestController
@RequestMapping("/api/employees/{employeeId}/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @PostMapping
    public ResponseEntity<PositionDto> assignPosition(@PathVariable String employeeId, @RequestBody @Valid CreatePositionCommand command) {
        PositionDto positionDto = positionService.assignPositionToEmployee(employeeId, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(positionDto);
    }
}
