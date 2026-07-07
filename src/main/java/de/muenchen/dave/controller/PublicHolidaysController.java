package de.muenchen.dave.controller;

import de.muenchen.dave.services.kalendertag.PublicHolidaysImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/public-holidays")
@RequiredArgsConstructor
@Tag(name = "Public holidays", description = "Management and import of public holidays")
public class PublicHolidaysController {

    private final PublicHolidaysImportService publicHolidaysImportService;

    @Operation(summary = "Import public holidays")
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @PostMapping(value = "/import")
    public ResponseEntity<Integer> loadPublicHolidaysForYear() {
        try {
            int result = publicHolidaysImportService.importForCurrentAndNextYear(true);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid configuration: " + e.getMessage());
        }
    }
}
