package de.muenchen.dave.domain.dtos.laden.drilldown;

import java.util.List;
import java.util.Map;

public record DrilldownDTO(
        List<FahrbeziehungKey> fahrbeziehungen, // ordered column groups
        List<ZeitIntervallRow> zeitintervalle,
        Map<FahrbeziehungKey, FahrbeziehungWerte> spaltensummen) {
}
