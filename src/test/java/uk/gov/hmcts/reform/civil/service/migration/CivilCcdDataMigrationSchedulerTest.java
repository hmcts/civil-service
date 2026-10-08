package uk.gov.hmcts.reform.civil.service.migration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.migration.CcdDataMigrationRunResult;
import uk.gov.hmcts.ccd.sdk.migration.CcdDataMigrationTask;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CivilCcdDataMigrationSchedulerTest {

    private final CcdDataMigrationTask migrationTask = mock(CcdDataMigrationTask.class);
    private final CivilCcdDataMigrationScheduler scheduler = new CivilCcdDataMigrationScheduler(migrationTask);

    @Test
    void runMigration_runsSdkMigrationTask() {
        CcdDataMigrationRunResult result = new CcdDataMigrationRunResult(true, 2, 3, 4, true, false);
        when(migrationTask.runMigration()).thenReturn(result);

        scheduler.runMigration();

        verify(migrationTask).runMigration();
    }
}
