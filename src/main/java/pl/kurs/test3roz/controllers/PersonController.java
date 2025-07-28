package pl.kurs.test3roz.controllers;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.commands.UpdatePersonCommand;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.filters.GetPersonFilter;
import pl.kurs.test3roz.views.PersonSummaryView;
import pl.kurs.test3roz.services.PersonService;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping
    public ResponseEntity<PersonDto> createPerson(@RequestBody @Valid CreatePersonCommand command) {
        PersonDto createdPerson = personService.createPerson(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
    }

    @PostMapping("/search") //tutaj mi się kurcze to nie do końca podoba, ale znalazłem, że dla wielu filtrów to najlepsza opcja żeby nie przekazywać ich wszystkich w url jeśli chcemy ich dużo (no i np na jakims otomoto tez tak robia wiec mam nadzieje ze git:))
    public ResponseEntity<Page<PersonSummaryView>> getFilteredPersons(@RequestBody GetPersonFilter filter,@PageableDefault(size = 30, page = 0, sort = "lastName", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(personService.getFilteredPersonSummaries(filter, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonDto> updatePerson(@PathVariable String id, @RequestBody @Valid UpdatePersonCommand command) {
        PersonDto updated = personService.updatePerson(id, command);
        return ResponseEntity.ok(updated);
    }
}