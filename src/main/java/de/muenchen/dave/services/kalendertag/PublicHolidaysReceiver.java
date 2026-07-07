package de.muenchen.dave.services.kalendertag;

import de.muenchen.dave.configuration.LogExecutionTime;
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
public class PublicHolidaysReceiver {

    private final PublicHolidaysImportService publicHolidaysImportService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    @LogExecutionTime
    public void checkPublicHolidaysOnStartup() {
        log.info("#checkPublicHolidaysOnStartup");
        publicHolidaysImportService.importForCurrentAndNextYear(false);
    }

    /**
     * Checks and updates public holidays monthly using the configured holiday source.
     */
    @Scheduled(cron = "${dave.publicholidays.cron}")
    @SchedulerLock(
            name = "loadPublicHolidaysCron",
            lockAtMostFor = "${dave.publicholidays.shedlock}",
            lockAtLeastFor = "${dave.publicholidays.shedlock}"
    )
    @Transactional
    @LogExecutionTime
    public void loadPublicHolidaysCron() {
        log.info("#loadPublicHolidaysCron");
        publicHolidaysImportService.importForCurrentAndNextYear(false);
    }
}
