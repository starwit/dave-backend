package de.muenchen.dave.services.kalendertag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.ConfigurationEntity;
import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.dtos.HolidaysDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.repositories.relationaldb.ConfigurationRepository;
import de.muenchen.dave.repositories.relationaldb.KalendertagRepository;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HolidaysImportServiceTest {

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private KalendertagRepository kalendertagRepository;

    @Mock
    private OpenHolidaysApiClient openHolidaysApiClient;

    @InjectMocks
    private HolidaysImportService holidaysImportService;

    @Test
    void loadAndSavePublicHolidaysForYearSetsTagestypAndCreatesMissingDates() {
        final LocalDate existingDate = LocalDate.of(2026, 1, 1);
        final LocalDate missingDate = LocalDate.of(2026, 5, 1);
        final ConfigurationEntity config = ConfigurationEntity.builder()
                .keyname(HolidaysImportService.CONFIG_VALUES.get(TagesTyp.SONNTAG_FEIERTAG))
                .valuefield("https://example.org/PublicHolidays?validFrom={validFrom}&validTo={validTo}&countryIsoCode=DE")
                .category("dave")
                .build();

        final Kalendertag existingKalendertag = new Kalendertag();
        existingKalendertag.setDatum(existingDate);
        existingKalendertag.setTagestyp(TagesTyp.WERKTAG_MO_FR);

        final HolidaysDTO existingHolidaysDTO = new HolidaysDTO();
        existingHolidaysDTO.setStartDate(existingDate);
        existingHolidaysDTO.setEndDate(existingDate);

        final HolidaysDTO missingHolidaysDTO = new HolidaysDTO();
        missingHolidaysDTO.setStartDate(missingDate);
        missingHolidaysDTO.setEndDate(missingDate);

        when(kalendertagRepository.existsByDatumBetweenAndTagestyp(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                TagesTyp.SONNTAG_FEIERTAG)).thenReturn(false);
        when(configurationRepository.findByKeyname(HolidaysImportService.CONFIG_VALUES.get(TagesTyp.SONNTAG_FEIERTAG))).thenReturn(config);
        when(openHolidaysApiClient.loadHolidays(any())).thenReturn(List.of(
                existingHolidaysDTO,
                missingHolidaysDTO));
        when(kalendertagRepository.findAllByDatumIn(List.of(existingDate, missingDate))).thenReturn(List.of(existingKalendertag));

        final AtomicReference<List<Kalendertag>> savedKalendertageRef = new AtomicReference<>();
        doAnswer(invocation -> {
            savedKalendertageRef.set(invocation.getArgument(0));
            return invocation.getArgument(0);
        }).when(kalendertagRepository).saveAll(any());

        final int result = holidaysImportService.loadAndSaveHolidaysForYear(TagesTyp.SONNTAG_FEIERTAG, 2026, true);

        assertThat(result).isEqualTo(2);

        verify(kalendertagRepository, times(1)).saveAll(any());

        final List<Kalendertag> savedKalendertage = savedKalendertageRef.get();
        assertThat(savedKalendertage).hasSize(2);
        assertThat(savedKalendertage)
                .extracting(Kalendertag::getDatum)
                .containsExactly(existingDate, missingDate);
        assertThat(savedKalendertage)
                .extracting(Kalendertag::getTagestyp)
                .containsOnly(TagesTyp.SONNTAG_FEIERTAG);
    }

    @Test
    void loadAndSavePublicHolidaysForYearUsesConfiguredTemplateUrl() {
        final ConfigurationEntity config = ConfigurationEntity.builder()
                .keyname(HolidaysImportService.CONFIG_VALUES.get(TagesTyp.SONNTAG_FEIERTAG))
                .valuefield("https://example.org/PublicHolidays?validFrom={validFrom}&validTo={validTo}&countryIsoCode=DE")
                .category("dave")
                .build();

        when(kalendertagRepository.existsByDatumBetweenAndTagestyp(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                TagesTyp.SONNTAG_FEIERTAG)).thenReturn(false);
        when(configurationRepository.findByKeyname(HolidaysImportService.CONFIG_VALUES.get(TagesTyp.SONNTAG_FEIERTAG))).thenReturn(config);
        when(openHolidaysApiClient.loadHolidays(any())).thenReturn(List.of());

        holidaysImportService.loadAndSaveHolidaysForYear(TagesTyp.SONNTAG_FEIERTAG, 2026, true);

        final ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(openHolidaysApiClient, times(1)).loadHolidays(uriCaptor.capture());
        final URI calledUri = uriCaptor.getValue();

        assertThat(calledUri.toString()).contains("validFrom=2026-01-01");
        assertThat(calledUri.toString()).contains("validTo=2026-12-31");
    }
}
