package de.muenchen.dave.services.kalendertag;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.dtos.PublicHolidaysDTO;
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
public class PublicHolidaysImportService {

    public static final String CONFIG_KEY_PUBLIC_HOLIDAYS_URL = "publicHolidaysApiUrl";

    static final String DEFAULT_PUBLIC_HOLIDAYS_URL = "https://openholidaysapi.org/PublicHolidays?countryIsoCode=DE&languageIsoCode=DE&subdivisionCode=DE-NI&validFrom={validFrom}&validTo={validTo}";

    private final ConfigurationRepository configurationRepository;

    private final KalendertagRepository kalendertagRepository;

    private final OpenHolidaysApiClient openHolidaysApiClient;

    @Transactional
    public int loadAndSavePublicHolidaysForYear(final int year) {
        final LocalDate validFrom = LocalDate.of(year, Month.JANUARY, 1);
        final LocalDate validTo = LocalDate.of(year, Month.DECEMBER, 31);

        final boolean dataExistsForYear = kalendertagRepository.existsByDatumBetween(validFrom, validTo);
        if (dataExistsForYear) {
            log.info("Public holidays for year {} already exist, skipping import", year);
            return 0;
        }

        final URI sourceUri = buildSourceUri(validFrom, validTo);
        final List<PublicHolidaysDTO> holidays = openHolidaysApiClient.loadPublicHolidays(sourceUri);

        final List<LocalDate> holidayDates = holidays
                .stream()
                .filter(Objects::nonNull)
                .flatMap(this::toDates)
                .filter(date -> !date.isBefore(validFrom) && !date.isAfter(validTo))
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        if (holidayDates.isEmpty()) {
            log.info("No public holidays found for year {} via {}", year, sourceUri);
            return 0;
        }

        final Map<LocalDate, Kalendertag> existingKalendertageByDate = new HashMap<>();
        kalendertagRepository.findAllByDatumIn(holidayDates)
                .forEach(kalendertag -> existingKalendertageByDate.put(kalendertag.getDatum(), kalendertag));

        final List<Kalendertag> kalendertageToSave = holidayDates
                .stream()
                .map(date -> {
                    final Kalendertag kalendertag = existingKalendertageByDate.getOrDefault(date, new Kalendertag());
                    kalendertag.setDatum(date);
                    kalendertag.setTagestyp(TagesTyp.SONNTAG_FEIERTAG);
                    return kalendertag;
                })
                .toList();

        kalendertagRepository.saveAll(kalendertageToSave);
        log.info("Saved {} public holiday entries for year {}", kalendertageToSave.size(), year);

        return kalendertageToSave.size();
    }

    public int importForCurrentAndNextYear() {
        final int currentYear = LocalDate.now().getYear();
        int totalImported = 0;
        for (int year = currentYear; year <= currentYear + 1; year++) {
            try {
                totalImported += loadAndSavePublicHolidaysForYear(year);
            } catch (final Exception exception) {
                log.error("Error while loading public holidays for year {}", year, exception);
            }
        }
        return totalImported;
    }

    private URI buildSourceUri(final LocalDate validFrom, final LocalDate validTo) {
        final var configuredSourceEntity = configurationRepository.findByKeyname(CONFIG_KEY_PUBLIC_HOLIDAYS_URL);
        final String configuredSource = configuredSourceEntity == null
                ? null
                : configuredSourceEntity.getValuefield();

        final String sourceUrl = StringUtils.isBlank(configuredSource)
                ? DEFAULT_PUBLIC_HOLIDAYS_URL
                : configuredSource;

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

    private Stream<LocalDate> toDates(final PublicHolidaysDTO holiday) {
        if (holiday.startDate() == null) {
            return Stream.empty();
        }

        final LocalDate endDate = holiday.endDate() == null
                ? holiday.startDate()
                : holiday.endDate();

        return Stream.iterate(
                holiday.startDate(),
                date -> !date.isAfter(endDate),
                date -> date.plusDays(1));
    }
}
