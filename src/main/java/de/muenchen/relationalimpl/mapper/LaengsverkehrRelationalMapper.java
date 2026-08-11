package de.muenchen.relationalimpl.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LaengsverkehrRelationalMapper {

    de.muenchen.dave.domain.analytics.Laengsverkehr elastic2analytics(@MappingTarget de.muenchen.dave.domain.analytics.Laengsverkehr analytics,
            de.muenchen.dave.domain.elasticsearch.Laengsverkehr elastic);
}
