package de.muenchen.dave.services.kalendertag;

import de.muenchen.dave.configuration.LogExecutionTime;
import de.muenchen.dave.domain.enums.TagesTyp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile({ "!konexternal && !prodexternal && !unittest" })
public class HolidaysReceiver {

    private final HolidaysImportService holidaysImportService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    @LogExecutionTime
    public void checkHolidaysOnStartup() {
        log.info("#checkHolidaysOnStartup");
        holidaysImportService.importForCurrentAndNextYear(false, TagesTyp.FEIERTAG);
        holidaysImportService.importForCurrentAndNextYear(false, TagesTyp.FERIEN);
    }

    /**
     * Checks and updates public holidays monthly using the configured holiday source.
     */
    @Scheduled(cron = "${dave.holidays.cron}")
    @SchedulerLock(
            name = "loadHolidaysCron",
            lockAtMostFor = "${dave.holidays.shedlock}",
            lockAtLeastFor = "${dave.holidays.shedlock}"
    )
    @Transactional
    @LogExecutionTime
    public void loadHolidaysCron() {
        log.info("#loadHolidaysCron");
        holidaysImportService.importForCurrentAndNextYear(false, TagesTyp.FEIERTAG);
        holidaysImportService.importForCurrentAndNextYear(false, TagesTyp.FERIEN);
    }
}
