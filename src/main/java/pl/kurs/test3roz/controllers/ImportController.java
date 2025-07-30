package pl.kurs.test3roz.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.test3roz.imports.ImportService;
import pl.kurs.test3roz.imports.ImportStatus;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping
    public ResponseEntity<String> importCsv(@RequestParam("file") @Valid MultipartFile file) {
        String importId = importService.importCsv(file);
        return ResponseEntity.accepted().body(importId);
    }

    @GetMapping("/status/{importId}")
    public ResponseEntity<ImportStatus> getStatus(@PathVariable String importId) {
        return ResponseEntity.ok(importService.getStatus(importId));
    }

}
