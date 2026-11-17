package de.muenchen.dave.domain.dtos;

import lombok.Data;

/**
 * Eine Zählung mit der Zählstelle, zu der sie gehört. Ermöglicht einem externen System, seine Zählungen anhand der
 * selbst vergebenen Projektnummer wiederzufinden, unabhängig von der Zählstelle.
 */
@Data
public class ZaehlungByProjektNummerDTO {

    private String id;

    private String projektNummer;

    private String status;

    private String zaehlstelleId;
}
