package de.muenchen.relationalimpl.mapper;

import de.muenchen.dave.domain.elasticsearch.Querungsverkehr;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QuerungsverkehrRelationalMapper {

    de.muenchen.dave.domain.analytics.Querungsverkehr elastic2analytics(@MappingTarget de.muenchen.dave.domain.analytics.Querungsverkehr analytics,
            Querungsverkehr elastic);
}
