package de.muenchen.dave.domain.dtos.laden.drilldown;

import java.util.Map;

// One row: one time interval, with a value block per movement
public record ZeitIntervallRow(
        String startUhrzeit,
        String endeUhrzeit,
        Map<VerkehrsbeziehungKey, VerkehrsbeziehungWerte> wertByVerkehrsbeziehung) {
}
