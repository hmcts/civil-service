package uk.gov.hmcts.reform.civil.scheduler.takecaseoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.TakeCaseOfflineSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TakeCaseOfflineSchedulerTest {

    @Mock
    private TakeCaseOfflineSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private TakeCaseOfflineScheduledTask takeCaseOfflineScheduledTask;
    @InjectMocks
    private TakeCaseOfflineScheduler scheduler;

    @Test
    void shouldRunTakeCaseOfflineTask() {
        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo("TakeCaseOffline");
        verify(scheduledTaskRunner).run(
            eq(TakeCaseOfflineScheduler.SCHEDULER_NAME),
            any(),
            eq(takeCaseOfflineScheduledTask)
        );
    }
}
