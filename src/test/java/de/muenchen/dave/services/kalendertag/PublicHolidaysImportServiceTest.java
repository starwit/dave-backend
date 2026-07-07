package de.muenchen.dave.services.kalendertag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.ConfigurationEntity;
import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.dtos.PublicHolidaysDTO;
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
class PublicHolidaysImportServiceTest {

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private KalendertagRepository kalendertagRepository;

    @Mock
    private OpenHolidaysApiClient openHolidaysApiClient;

    @InjectMocks
    private PublicHolidaysImportService publicHolidaysImportService;

    @Test
    void loadAndSavePublicHolidaysForYearSetsTagestypAndCreatesMissingDates() {
        final LocalDate existingDate = LocalDate.of(2026, 1, 1);
        final LocalDate missingDate = LocalDate.of(2026, 5, 1);

        final Kalendertag existingKalendertag = new Kalendertag();
        existingKalendertag.setDatum(existingDate);
        existingKalendertag.setTagestyp(TagesTyp.WERKTAG_MO_FR);

        when(kalendertagRepository.existsByDatumBetween(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))).thenReturn(false);
        when(configurationRepository.findByKeyname(PublicHolidaysImportService.CONFIG_KEY_PUBLIC_HOLIDAYS_URL)).thenReturn(null);
        when(openHolidaysApiClient.loadPublicHolidays(any())).thenReturn(List.of(
                new PublicHolidaysDTO(existingDate, existingDate),
                new PublicHolidaysDTO(missingDate, missingDate)));
        when(kalendertagRepository.findAllByDatumIn(List.of(existingDate, missingDate))).thenReturn(List.of(existingKalendertag));

        final AtomicReference<List<Kalendertag>> savedKalendertageRef = new AtomicReference<>();
        doAnswer(invocation -> {
            savedKalendertageRef.set(invocation.getArgument(0));
            return invocation.getArgument(0);
        }).when(kalendertagRepository).saveAll(any());

        final int result = publicHolidaysImportService.loadAndSavePublicHolidaysForYear(2026, true);

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
                .keyname(PublicHolidaysImportService.CONFIG_KEY_PUBLIC_HOLIDAYS_URL)
                .valuefield("https://example.org/PublicHolidays?validFrom={validFrom}&validTo={validTo}&countryIsoCode=DE")
                .category("dave")
                .build();

        when(kalendertagRepository.existsByDatumBetween(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))).thenReturn(false);
        when(configurationRepository.findByKeyname(PublicHolidaysImportService.CONFIG_KEY_PUBLIC_HOLIDAYS_URL)).thenReturn(config);
        when(openHolidaysApiClient.loadPublicHolidays(any())).thenReturn(List.of());

        publicHolidaysImportService.loadAndSavePublicHolidaysForYear(2026, true);

        final ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(openHolidaysApiClient, times(1)).loadPublicHolidays(uriCaptor.capture());
        final URI calledUri = uriCaptor.getValue();

        assertThat(calledUri.toString()).contains("validFrom=2026-01-01");
        assertThat(calledUri.toString()).contains("validTo=2026-12-31");
    }
}
