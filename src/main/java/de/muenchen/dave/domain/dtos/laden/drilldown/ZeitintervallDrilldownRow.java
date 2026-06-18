package de.muenchen.dave.domain.dtos.laden.drilldown;

import java.util.List;
import java.util.Map;

// One row in the drill-down table: a single 15-min interval
public record ZeitintervallDrilldownRow(
        List<FahrbeziehungKey> fahrbeziehungen, // ordered column groups
        List<ZeitIntervallRow> rows,
        Map<FahrbeziehungKey, FahrbeziehungWerte> spaltensummen) {
}
