package de.muenchen.dave.domain.dtos;

import java.time.LocalDate;
import lombok.Data;

@Data
public class HolidaysDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}
