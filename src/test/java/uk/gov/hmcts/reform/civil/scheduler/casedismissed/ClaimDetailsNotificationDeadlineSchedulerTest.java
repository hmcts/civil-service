package uk.gov.hmcts.reform.civil.scheduler.casedismissed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.ClaimDetailsNotificationDeadlineSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClaimDetailsNotificationDeadlineSchedulerTest {

    @Mock
    private ClaimDetailsNotificationDeadlineSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private CaseDismissedScheduledTask caseDismissedScheduledTask;
    @InjectMocks
    private ClaimDetailsNotificationDeadlineScheduler scheduler;

    @Test
    void shouldRunClaimDetailsNotificationDeadlineTask() {
        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(ClaimDetailsNotificationDeadlineScheduler.SCHEDULER_NAME);
        verify(scheduledTaskRunner).run(
            eq(ClaimDetailsNotificationDeadlineScheduler.SCHEDULER_NAME),
            any(),
            eq(caseDismissedScheduledTask)
        );
    }
}
