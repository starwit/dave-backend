package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VerkehrsbeziehungMapper {

    /**
     * bearbeite auf bean (für Kreuzung)
     *
     * @param dto BearbeiteVerkehrsbeziehungDTO
     * @return gemappte Verkehrsbeziehung
     */
    @Mapping(target = "hochrechnungsfaktor.version", source = "hochrechnungsfaktor.entityVersion")
    Verkehrsbeziehung bearbeiteVerkehrsbeziehungDto2bean(BearbeiteVerkehrsbeziehungDTO dto);

    /**
     * bean auf bearbeite (für Kreuzung)
     *
     * @param bean Verkehrsbeziehung
     * @return gemapptes BearbeiteVerkehrsbeziehungDTO
     */
    @Mapping(target = "hochrechnungsfaktor.entityVersion", source = "hochrechnungsfaktor.version")
    BearbeiteVerkehrsbeziehungDTO bean2bearbeiteVerkehrsbeziehungDto(Verkehrsbeziehung bean);

}
