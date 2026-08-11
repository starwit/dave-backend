package de.muenchen.relationalimpl.mapper;

import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Laengsverkehr;
import de.muenchen.dave.domain.elasticsearch.Querungsverkehr;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.util.DaveConstants;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ZaehlungRelationalMapper {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DaveConstants.DATE_FORMAT);

    @Mapping(target = "knotenarme", ignore = true)
    @Mapping(target = "verkehrsbeziehungen", ignore = true)
    @Mapping(target = "querungsverkehr", ignore = true)
    @Mapping(target = "laengsverkehr", ignore = true)
    de.muenchen.dave.domain.analytics.Zaehlung elastic2analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlung analytics,
            Zaehlung elastic, @Context VerkehrsbeziehungRelationalMapper verkehrsbeziehungMapper,
            @Context QuerungsverkehrRelationalMapper querungsverkehrMapper, @Context LaengsverkehrRelationalMapper laengsverkehrMapper);

    @BeforeMapping
    default void beforeElastic2Analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlung analytics,
            @Context QuerungsverkehrRelationalMapper querungsverkehrMapper, @Context LaengsverkehrRelationalMapper laengsverkehrMapper) {
        // Ensure all list fields are mutable ArrayLists to avoid UnsupportedOperationException
        // when MapStruct tries to clear them during mapping
        if (analytics.getSuchwoerter() == null) {
            analytics.setSuchwoerter(new ArrayList<>());
        } else if (!(analytics.getSuchwoerter() instanceof ArrayList)) {
            analytics.setSuchwoerter(new ArrayList<>(analytics.getSuchwoerter()));
        }

        if (analytics.getCustomSuchwoerter() == null) {
            analytics.setCustomSuchwoerter(new ArrayList<>());
        } else if (!(analytics.getCustomSuchwoerter() instanceof ArrayList)) {
            analytics.setCustomSuchwoerter(new ArrayList<>(analytics.getCustomSuchwoerter()));
        }

        if (analytics.getGeographie() == null) {
            analytics.setGeographie(new ArrayList<>());
        } else if (!(analytics.getGeographie() instanceof ArrayList)) {
            analytics.setGeographie(new ArrayList<>(analytics.getGeographie()));
        }

        if (analytics.getKategorien() == null) {
            analytics.setKategorien(new ArrayList<>());
        } else if (!(analytics.getKategorien() instanceof ArrayList)) {
            analytics.setKategorien(new ArrayList<>(analytics.getKategorien()));
        }
    }

    @AfterMapping
    default void afterElastic2Analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlung analytics,
            Zaehlung elastic, @Context VerkehrsbeziehungRelationalMapper verkehrsbeziehungMapper,
            @Context QuerungsverkehrRelationalMapper querungsverkehrMapper, @Context LaengsverkehrRelationalMapper laengsverkehrMapper) {

        // Initialize collection if null
        if (analytics.getKnotenarme() == null) {
            analytics.setKnotenarme(new ArrayList<>());
        }

        if (elastic.getKnotenarme() == null || elastic.getKnotenarme().isEmpty()) {
            analytics.getKnotenarme().clear();
            return;
        }

        // Create a map of existing knotenarme by ID for quick lookup
        Map<UUID, de.muenchen.dave.domain.analytics.Knotenarm> existingKnotenarmeMap = new HashMap<>();
        for (de.muenchen.dave.domain.analytics.Knotenarm k : analytics.getKnotenarme()) {
            if (k.getId() != null) {
                existingKnotenarmeMap.put(k.getId(), k);
            }
        }

        // Process incoming knotenarme
        List<de.muenchen.dave.domain.analytics.Knotenarm> updatedKnotenarme = new ArrayList<>();
        for (Knotenarm elasticKnotenarm : elastic.getKnotenarme()) {
            de.muenchen.dave.domain.analytics.Knotenarm analyticsKnotenarm;

            if (elasticKnotenarm.getId() != null && !elasticKnotenarm.getId().isBlank()) {
                UUID knotenarmId = UUID.fromString(elasticKnotenarm.getId());
                // Update existing knotenarm
                analyticsKnotenarm = existingKnotenarmeMap.get(knotenarmId);
                if (analyticsKnotenarm == null) {
                    analyticsKnotenarm = new de.muenchen.dave.domain.analytics.Knotenarm();
                }
            } else {
                // Create new knotenarm
                analyticsKnotenarm = new de.muenchen.dave.domain.analytics.Knotenarm();
            }

            // Map properties from elastic to analytics
            if (elasticKnotenarm.getId() != null && !elasticKnotenarm.getId().isBlank()) {
                analyticsKnotenarm.setId(UUID.fromString(elasticKnotenarm.getId()));
                analyticsKnotenarm.setVersion(elasticKnotenarm.getVersion());
            }
            analyticsKnotenarm.setNummer(elasticKnotenarm.getNummer());
            analyticsKnotenarm.setStrassenname(elasticKnotenarm.getStrassenname());
            analyticsKnotenarm.setFilename(elasticKnotenarm.getFilename());

            // Set bidirectional relationship
            analyticsKnotenarm.setZaehlung(analytics);
            updatedKnotenarme.add(analyticsKnotenarm);
        }

        // Clear and replace the collection content (preserves Hibernate wrapper)
        analytics.getKnotenarme().clear();
        analytics.getKnotenarme().addAll(updatedKnotenarme);

        // Initialize collection if null
        if (analytics.getVerkehrsbeziehungen() == null) {
            analytics.setVerkehrsbeziehungen(new ArrayList<>());
        }

        if (elastic.getVerkehrsbeziehungen() == null || elastic.getVerkehrsbeziehungen().isEmpty()) {
            analytics.getVerkehrsbeziehungen().clear();
        } else {
            // Create a map of existing verkehrsbeziehungen by ID for quick lookup
            Map<UUID, de.muenchen.dave.domain.analytics.Verkehrsbeziehung> existingVerkehrsbeziehungenMap = new HashMap<>();
            for (de.muenchen.dave.domain.analytics.Verkehrsbeziehung f : analytics.getVerkehrsbeziehungen()) {
                if (f.getId() != null) {
                    existingVerkehrsbeziehungenMap.put(f.getId(), f);
                }
            }

            // Process incoming verkehrsbeziehungen
            List<de.muenchen.dave.domain.analytics.Verkehrsbeziehung> updatedVerkehrsbeziehungen = new ArrayList<>();
            for (Verkehrsbeziehung elasticVerkehrsbeziehung : elastic.getVerkehrsbeziehungen()) {
                de.muenchen.dave.domain.analytics.Verkehrsbeziehung analyticsVerkehrsbeziehung;

                if (elasticVerkehrsbeziehung.getId() != null && !elasticVerkehrsbeziehung.getId().isBlank()) {
                    UUID verkehrsbeziehungId = UUID.fromString(elasticVerkehrsbeziehung.getId());
                    // Update existing verkehrsbeziehung
                    analyticsVerkehrsbeziehung = existingVerkehrsbeziehungenMap.get(verkehrsbeziehungId);
                    if (analyticsVerkehrsbeziehung == null) {
                        analyticsVerkehrsbeziehung = new de.muenchen.dave.domain.analytics.Verkehrsbeziehung();
                    }
                } else {
                    // Create new verkehrsbeziehung
                    analyticsVerkehrsbeziehung = new de.muenchen.dave.domain.analytics.Verkehrsbeziehung();
                }
                // Map properties from elastic to analytics
                analyticsVerkehrsbeziehung = verkehrsbeziehungMapper.elastic2analytics(analyticsVerkehrsbeziehung, elasticVerkehrsbeziehung);
                // Set bidirectional relationship
                analyticsVerkehrsbeziehung.setZaehlung(analytics);
                updatedVerkehrsbeziehungen.add(analyticsVerkehrsbeziehung);
            }

            // Clear and replace the collection content (preserves Hibernate wrapper)
            analytics.getVerkehrsbeziehungen().clear();
            analytics.getVerkehrsbeziehungen().addAll(updatedVerkehrsbeziehungen);
        }

        // Initialize collection if null
        if (analytics.getQuerungsverkehr() == null) {
            analytics.setQuerungsverkehr(new ArrayList<>());
        }

        if (elastic.getQuerungsverkehr() == null || elastic.getQuerungsverkehr().isEmpty()) {
            analytics.getQuerungsverkehr().clear();
        } else {
            // Create a map of existing querungsverkehr by ID for quick lookup
            Map<UUID, de.muenchen.dave.domain.analytics.Querungsverkehr> existingQuerungsverkehrMap = new HashMap<>();
            for (de.muenchen.dave.domain.analytics.Querungsverkehr f : analytics.getQuerungsverkehr()) {
                if (f.getId() != null) {
                    existingQuerungsverkehrMap.put(f.getId(), f);
                }
            }

            // Process incoming querungsverkehr
            List<de.muenchen.dave.domain.analytics.Querungsverkehr> updatedQuerungsverkehr = new ArrayList<>();
            for (Querungsverkehr elasticQuerungsverkehr : elastic.getQuerungsverkehr()) {
                de.muenchen.dave.domain.analytics.Querungsverkehr analyticsQuerungsverkehr;

                if (elasticQuerungsverkehr.getId() != null && !elasticQuerungsverkehr.getId().isBlank()) {
                    UUID querungsverkehrId = UUID.fromString(elasticQuerungsverkehr.getId());
                    // Update existing querungsverkehr
                    analyticsQuerungsverkehr = existingQuerungsverkehrMap.get(querungsverkehrId);
                    if (analyticsQuerungsverkehr == null) {
                        analyticsQuerungsverkehr = new de.muenchen.dave.domain.analytics.Querungsverkehr();
                    }
                } else {
                    // Create new querungsverkehr
                    analyticsQuerungsverkehr = new de.muenchen.dave.domain.analytics.Querungsverkehr();
                }
                // Map properties from elastic to analytics
                analyticsQuerungsverkehr = querungsverkehrMapper.elastic2analytics(analyticsQuerungsverkehr, elasticQuerungsverkehr);
                // Set bidirectional relationship
                analyticsQuerungsverkehr.setZaehlung(analytics);
                updatedQuerungsverkehr.add(analyticsQuerungsverkehr);
            }

            // Clear and replace the collection content (preserves Hibernate wrapper)
            analytics.getQuerungsverkehr().clear();
            analytics.getQuerungsverkehr().addAll(updatedQuerungsverkehr);
        }

        // Initialize collection if null
        if (analytics.getLaengsverkehr() == null) {
            analytics.setLaengsverkehr(new ArrayList<>());
        }

        if (elastic.getLaengsverkehr() == null || elastic.getLaengsverkehr().isEmpty()) {
            analytics.getLaengsverkehr().clear();
            return;
        }

        // Create a map of existing laengsverkehr by ID for quick lookup
        Map<UUID, de.muenchen.dave.domain.analytics.Laengsverkehr> existingLaengsverkehrMap = new HashMap<>();
        for (de.muenchen.dave.domain.analytics.Laengsverkehr f : analytics.getLaengsverkehr()) {
            if (f.getId() != null) {
                existingLaengsverkehrMap.put(f.getId(), f);
            }
        }

        // Process incoming laengsverkehr
        List<de.muenchen.dave.domain.analytics.Laengsverkehr> updatedLaengsverkehr = new ArrayList<>();
        for (Laengsverkehr elasticLaengsverkehr : elastic.getLaengsverkehr()) {
            de.muenchen.dave.domain.analytics.Laengsverkehr analyticsLaengsverkehr;

            if (elasticLaengsverkehr.getId() != null && !elasticLaengsverkehr.getId().isBlank()) {
                UUID laengsverkehrId = UUID.fromString(elasticLaengsverkehr.getId());
                // Update existing laengsverkehr
                analyticsLaengsverkehr = existingLaengsverkehrMap.get(laengsverkehrId);
                if (analyticsLaengsverkehr == null) {
                    analyticsLaengsverkehr = new de.muenchen.dave.domain.analytics.Laengsverkehr();
                }
            } else {
                // Create new laengsverkehr
                analyticsLaengsverkehr = new de.muenchen.dave.domain.analytics.Laengsverkehr();
            }
            // Map properties from elastic to analytics
            analyticsLaengsverkehr = laengsverkehrMapper.elastic2analytics(analyticsLaengsverkehr, elasticLaengsverkehr);
            // Set bidirectional relationship
            analyticsLaengsverkehr.setZaehlung(analytics);
            updatedLaengsverkehr.add(analyticsLaengsverkehr);
        }

        // Clear and replace the collection content (preserves Hibernate wrapper)
        analytics.getLaengsverkehr().clear();
        analytics.getLaengsverkehr().addAll(updatedLaengsverkehr);
    }

    Iterable<de.muenchen.dave.domain.analytics.Zaehlung> elasticlist2analyticslist(Iterable<? extends Zaehlung> elastic);

    Iterable<Zaehlung> analyticslist2elasticlist(Iterable<? extends de.muenchen.dave.domain.analytics.Zaehlung> elastic);

    Zaehlung analytics2elastic(de.muenchen.dave.domain.analytics.Zaehlung analytics);
}
