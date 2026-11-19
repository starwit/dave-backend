package de.muenchen.dave.domain.dtos.bearbeiten;

import java.util.List;
import lombok.Data;

@Data
public class BearbeiteZaehlstelleDTO {

    String id;

    String nummer;

    int stadtbezirkNummer;

    String stadtbezirk;

    double lat;

    double lng;

    List<String> customSuchwoerter;

    List<BearbeiteZaehlungDTO> zaehlungen;

    String kommentar;

    Boolean sichtbarDatenportal;

}
