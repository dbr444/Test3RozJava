package pl.kurs.test3roz.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.test3roz.imports.models.ImportIdDto;
import pl.kurs.test3roz.imports.ImportService;
import pl.kurs.test3roz.imports.models.ImportStatusDto;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping
    public ResponseEntity<ImportIdDto> importCsv(@RequestParam("file") @Valid MultipartFile file) {
        ImportIdDto importIdDto = importService.importCsv(file);
        return ResponseEntity.accepted().body(importIdDto);
    }

    @GetMapping("/status/{importId}")
    public ResponseEntity<ImportStatusDto> getStatus(@PathVariable String importId) {
        return ResponseEntity.ok(importService.getStatusDto(importId));
    }
}
