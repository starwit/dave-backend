package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.external.DetectionDTO;
import java.time.Instant;
import java.time.LocalDateTime;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DetectorMapper {

    @Mapping(target = "von", source = "fahrbeziehung.von")
    @Mapping(target = "nach", source = "fahrbeziehung.nach")
    @Mapping(target = "startUhrzeit", ignore = true)
    @Mapping(target = "endeUhrzeit", ignore = true)
    DetectionDTO bean2DetectionDTO(Zeitintervall zi);

    @Mapping(target = "fahrbeziehung.von", source = "von")
    @Mapping(target = "fahrbeziehung.nach", source = "nach")
    @Mapping(target = "type", constant = "STUNDE_VIERTEL")
    @Mapping(target = "fahrbeziehungId", ignore = true)
    @Mapping(target = "sortingIndex", ignore = true)
    @Mapping(target = "zaehlungId", source = "zaehlungId")
    @Mapping(target = "hochrechnung", ignore = true)
    @Mapping(target = "startUhrzeit", ignore = true)
    @Mapping(target = "endeUhrzeit", ignore = true)
    Zeitintervall detectionDTO2Bean(DetectionDTO dto);

    @AfterMapping
    default void toDTO(@MappingTarget DetectionDTO dto, Zeitintervall zi) {
        LocalDateTime dateTime = zi.getStartUhrzeit();
        if (dateTime != null) {
            dto.setStartUhrzeit(dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
        } else {
            dto.setStartUhrzeit(null);
        }

        dateTime = zi.getEndeUhrzeit();
        if (dateTime != null) {
            dto.setEndeUhrzeit(dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
        } else {
            dto.setEndeUhrzeit(null);
        }
    }

    @AfterMapping
    default void toBean(@MappingTarget Zeitintervall zi, DetectionDTO dto) {
        Instant instant = dto.getStartUhrzeit();
        if (instant != null) {
            zi.setStartUhrzeit(LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()));
        } else {
            zi.setStartUhrzeit(null);
        }

        instant = dto.getEndeUhrzeit();
        if (instant != null) {
            zi.setEndeUhrzeit(LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()));
        } else {
            zi.setEndeUhrzeit(null);
        }
    }

}
