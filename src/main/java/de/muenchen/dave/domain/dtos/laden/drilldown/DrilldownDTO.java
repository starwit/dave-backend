package de.muenchen.dave.domain.dtos.laden.drilldown;

import java.util.List;
import java.util.Map;

public record DrilldownDTO(
        List<VerkehrsbeziehungKey> verkehrsbeziehung, // ordered column groups
        List<ZeitIntervallRow> zeitintervalle,
        Map<VerkehrsbeziehungKey, VerkehrsbeziehungWerte> spaltensummen) {
}
