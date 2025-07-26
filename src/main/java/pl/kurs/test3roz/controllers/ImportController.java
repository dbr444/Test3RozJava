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
    public ResponseEntity<Void> importCsv(@RequestParam("file") @Valid MultipartFile file) {
        importService.importCsv(file);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/status")
    public ResponseEntity<ImportStatus> getStatus() {
        return ResponseEntity.ok(importService.getStatus());
    }
}