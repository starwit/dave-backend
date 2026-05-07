package de.muenchen.dave.domain.dtos.external;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class DetectionDTO {

    @NotNull
    UUID zaehlungId;

    @NotNull
    Instant startUhrzeit;

    @NotNull
    Instant endeUhrzeit;

    Integer pkw;

    Integer lkw;

    Integer lastzuege;

    Integer busse;

    Integer kraftraeder;

    Integer fahrradfahrer;

    Integer fussgaenger;

    @NotNull
    Integer von;

    @NotNull
    Integer nach;

}
