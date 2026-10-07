package uk.gov.hmcts.reform.civil.service.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.migration.CcdDataMigrationRunResult;
import uk.gov.hmcts.ccd.sdk.migration.CcdDataMigrationTask;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "migration.ccd-data.enabled", havingValue = "true")
public class CivilCcdDataMigrationScheduler {

    private final CcdDataMigrationTask migrationTask;

    @Scheduled(cron = "${migration.ccd-data.cron}")
    public void runMigration() {
        CcdDataMigrationRunResult result = migrationTask.runMigration();

        log.info(
            "Civil CCD data migration result lockAcquired={} batches={} cases={} events={} caughtUp={} timeLimited={}",
            result.lockAcquired(),
            result.batchesProcessed(),
            result.casesProcessed(),
            result.eventsProcessed(),
            result.caughtUp(),
            result.stoppedByTimeLimit()
        );
    }
}
