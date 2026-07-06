package de.muenchen.dave.domain.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PublicHolidaysDTO(
        LocalDate startDate,
        LocalDate endDate) {
}
