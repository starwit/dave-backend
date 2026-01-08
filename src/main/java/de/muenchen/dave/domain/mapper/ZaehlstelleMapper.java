package de.muenchen.dave.domain.mapper;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import de.muenchen.dave.domain.dtos.LeseZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.LeseZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.external.ExternalZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungZaehlstelleKoordinateDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlstelleVisumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlstelleWithUnreadMessageDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungWithUnreadMessageDTO;
import de.muenchen.dave.domain.dtos.suche.SucheZaehlstelleSuggestDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ZaehlstelleMapper {

    BearbeiteZaehlstelleDTO bean2bearbeiteDto(Zaehlstelle bean, @Context StadtbezirkMapper stadtbezirkMapper);

    @Mapping(target = "zaehlungen", ignore = true)
    LadeZaehlstelleVisumDTO bean2ladeVisumDto(Zaehlstelle bean);

    Zaehlstelle bearbeiteDto2bean(BearbeiteZaehlstelleDTO dto, @Context StadtbezirkMapper stadtbezirkMapper);

    @AfterMapping
    default void toZaehlstelle(@MappingTarget Zaehlstelle bean, BearbeiteZaehlstelleDTO dto, @Context StadtbezirkMapper stadtbezirkMapper) {
        bean.setPunkt(new GeoPoint(dto.getLat(), dto.getLng()));

        if (dto.getPunkt() != null && dto.getPunkt().getLat() > 0 && dto.getPunkt().getLon() > 0) {
            bean.setPunkt(dto.getPunkt());
        }
        bean.setStadtbezirk(stadtbezirkMapper.bezeichnungOf(bean.getStadtbezirkNummer()));
    }

    SucheZaehlstelleSuggestDTO bean2SucheZaehlstelleSuggestDto(Zaehlstelle bean);

    @AfterMapping
    default void toSucheZaehlstelleSuggestDto(@MappingTarget SucheZaehlstelleSuggestDTO dto, Zaehlstelle bean) {
        dto.setText(bean.getNummer() + StringUtils.SPACE + bean.getStadtbezirk());
    }

    LeseZaehlstelleDTO bean2LeseZaehlstelleDto(Zaehlstelle bean);

    LadeZaehlstelleWithUnreadMessageDTO bean2LadeZaehlstelleWithUnreadMessageDTO(Zaehlstelle bean);

    @AfterMapping
    default void toLeseZaehlstelleDto(@MappingTarget LeseZaehlstelleDTO dto, Zaehlstelle bean) {
        dto.setLat(bean.getPunkt().getLat());
        dto.setLng(bean.getPunkt().getLon());
    }

    ExternalZaehlstelleDTO bean2ExternalDto(Zaehlstelle bean);

    LadeAuswertungZaehlstelleKoordinateDTO bean2LadeAuswertungZaehlstelleKoordinateDto(Zaehlstelle bean);

    @AfterMapping
    default void toLadeAuswertungZaehlstelleKoordinateDto(@MappingTarget LadeAuswertungZaehlstelleKoordinateDTO dto, Zaehlstelle bean) {
        dto.setLat(bean.getPunkt().getLat());
        dto.setLng(bean.getPunkt().getLon());
        final Zaehlung newestZaehlung = bean.getZaehlungen().stream()
                .max(Comparator.comparing(Zaehlung::getDatum))
                .orElse(null);
        dto.setLetzteZaehlung(newestZaehlung != null ? newestZaehlung.getDatum() : null);
    }

    LeseZaehlungDTO bean2LeseDto(Zaehlung bean);

    LadeZaehlungWithUnreadMessageDTO bean2LadeZaehlungWithUnreadMessageDTO(Zaehlung bean);

    @AfterMapping
    default void toLeseZaehlungDTO(@MappingTarget LeseZaehlungDTO dto, Zaehlung bean) {
        dto.setLat(bean.getPunkt().getLat());
        dto.setLng(bean.getPunkt().getLon());
    }

}
