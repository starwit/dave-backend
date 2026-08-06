package de.muenchen.relationalimpl.mapper;

import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VerkehrsbeziehungRelationalMapper {

    de.muenchen.dave.domain.analytics.Verkehrsbeziehung elastic2analytics(@MappingTarget de.muenchen.dave.domain.analytics.Verkehrsbeziehung analytics,
            Verkehrsbeziehung elastic);
}
