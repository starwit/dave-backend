package de.muenchen.dave.controller;

import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.services.kalendertag.HolidaysImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/holidays")
@RequiredArgsConstructor
@Tag(name = "Holidays", description = "Management and import of holidays")
public class HolidaysController {

    private final HolidaysImportService holidaysImportService;

    @Operation(summary = "Import public holidays")
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @GetMapping(value = "/import-public-holidays")
    public ResponseEntity<Integer> loadPublicHolidaysForYear() {
        try {
            int result = holidaysImportService.importForCurrentAndNextYear(true, TagesTyp.SONNTAG_FEIERTAG);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid configuration: " + e.getMessage());
        }
    }

    @Operation(summary = "Import school holidays")
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @GetMapping(value = "/import-school-holidays")
    public ResponseEntity<Integer> loadSchoolHolidaysForYear() {
        try {
            int result = holidaysImportService.importForCurrentAndNextYear(true, TagesTyp.FERIEN);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid configuration: " + e.getMessage());
        }
    }
}
