package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HolidayOptions {
    ONLY_SCHOOLHOLIDAYS("Nur Schulferien"),
    WITH_SCHOOLHOLIDAYS("Mit Schulferien"),
    NO_SCHOOLHOLIDAYS("Keine Schulferien");

    private final String beschreibung;
}
