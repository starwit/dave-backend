package de.muenchen.dave.domain.dtos.laden.drilldown;

// One cell: all vehicle counts for a single movement in a single interval
public record FahrbeziehungWerte(
        int pkw,
        int lkw,
        int lastzuege,
        int busse,
        int kraftraeder,
        int fahrradfahrer,
        int fussgaenger) {
}
