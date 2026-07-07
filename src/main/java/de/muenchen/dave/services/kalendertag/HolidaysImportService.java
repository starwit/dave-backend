package de.muenchen.dave.services.kalendertag;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.dtos.HolidaysDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.repositories.relationaldb.ConfigurationRepository;
import de.muenchen.dave.repositories.relationaldb.KalendertagRepository;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidaysImportService {

    public static final Map<TagesTyp, String> CONFIG_VALUES = Map.of(
            TagesTyp.SONNTAG_FEIERTAG, "publicHolidaysApiUrl",
            TagesTyp.FERIEN, "schoolHolidaysApiUrl");

    private final ConfigurationRepository configurationRepository;

    private final KalendertagRepository kalendertagRepository;

    private final OpenHolidaysApiClient openHolidaysApiClient;

    @Transactional
    public int loadAndSaveHolidaysForYear(final TagesTyp tagesTyp, final int year, boolean override) {
        final LocalDate validFrom = LocalDate.of(year, Month.JANUARY, 1);
        final LocalDate validTo = LocalDate.of(year, Month.DECEMBER, 31);

        final boolean dataExistsForYear = kalendertagRepository.existsByDatumBetweenAndTagestyp(validFrom, validTo, tagesTyp);
        if (dataExistsForYear && !override) {
            log.info("{} for year {} already exist, skipping import", tagesTyp.getBeschreibung(), year);
            return 0;
        } else if (dataExistsForYear) {
            log.info("{} for year {} already exist, overriding existing data", tagesTyp.getBeschreibung(), year);
            kalendertagRepository.deleteAllByDatumBetweenAndTagestyp(validFrom, validTo, tagesTyp);
        }

        String sourceUriConfig = CONFIG_VALUES.get(tagesTyp);
        final URI sourceUri = buildSourceUri(validFrom, validTo, sourceUriConfig);
        if (sourceUri == null) {
            log.info("No source URL configured for {}. Import skipped.", sourceUriConfig);
            return 0;
        }

        final List<HolidaysDTO> holidays = openHolidaysApiClient.loadHolidays(sourceUri);

        final List<LocalDate> holidayDates = holidays
                .stream()
                .filter(Objects::nonNull)
                .flatMap(this::toDates)
                .filter(date -> !date.isBefore(validFrom) && !date.isAfter(validTo))
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        if (holidayDates.isEmpty()) {
            log.info("No {} found for year {} via {}", tagesTyp.getBeschreibung(), year, sourceUri);
            return 0;
        }

        if (override) {
            log.info("Overriding existing {} entries for year {}", tagesTyp.getBeschreibung(), year);
            kalendertagRepository.deleteAllByDatumBetweenAndTagestyp(validFrom, validTo, tagesTyp);
        }

        final Map<LocalDate, Kalendertag> existingKalendertageByDate = new HashMap<>();
        kalendertagRepository.findAllByDatumIn(holidayDates)
                .forEach(kalendertag -> existingKalendertageByDate.put(kalendertag.getDatum(), kalendertag));

        final List<Kalendertag> kalendertageToSave = holidayDates
                .stream()
                .map(date -> {
                    final Kalendertag kalendertag = existingKalendertageByDate.getOrDefault(date, new Kalendertag());
                    kalendertag.setDatum(date);
                    kalendertag.setTagestyp(tagesTyp);
                    return kalendertag;
                })
                .toList();

        kalendertagRepository.saveAll(kalendertageToSave);
        log.info("Saved {} {} entries for year {}", kalendertageToSave.size(), tagesTyp.getBeschreibung(), year);

        return kalendertageToSave.size();
    }

    @Transactional
    public int importForCurrentAndNextYear(boolean override, TagesTyp tagesTyp) {
        final int currentYear = LocalDate.now().getYear();
        int totalImported = 0;
        for (int year = currentYear; year <= currentYear + 1; year++) {
            try {
                totalImported += loadAndSaveHolidaysForYear(tagesTyp, year, override);
            } catch (final Exception exception) {
                log.error("Error while loading holidays for year {}", year, exception);
            }
        }
        return totalImported;
    }

    private URI buildSourceUri(final LocalDate validFrom, final LocalDate validTo, final String configKeyName) {
        final var configuredSourceEntity = configurationRepository.findByKeyname(configKeyName);
        final String configuredSource = configuredSourceEntity == null
                ? null
                : configuredSourceEntity.getValuefield();

        final String sourceUrl = StringUtils.isBlank(configuredSource)
                ? null
                : configuredSource;

        if (StringUtils.isBlank(sourceUrl)) {
            return null;
        }

        final boolean hasTemplateVariables = sourceUrl.contains("{validFrom}") || sourceUrl.contains("{validTo}");
        if (hasTemplateVariables) {
            return UriComponentsBuilder
                    .fromUriString(sourceUrl)
                    .buildAndExpand(Map.of(
                            "validFrom", validFrom,
                            "validTo", validTo))
                    .toUri();
        }

        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(sourceUrl);
        final var queryParams = uriBuilder.build().getQueryParams();
        if (!queryParams.containsKey("validFrom")) {
            uriBuilder.queryParam("validFrom", validFrom);
        }
        if (!queryParams.containsKey("validTo")) {
            uriBuilder.queryParam("validTo", validTo);
        }
        return uriBuilder.build().toUri();
    }

    private Stream<LocalDate> toDates(final HolidaysDTO holiday) {
        if (holiday.getStartDate() == null) {
            return Stream.empty();
        }

        final LocalDate endDate = holiday.getEndDate() == null
                ? holiday.getStartDate()
                : holiday.getEndDate();

        return Stream.iterate(
                holiday.getStartDate(),
                date -> !date.isAfter(endDate),
                date -> date.plusDays(1));
    }
}
