package uk.gov.hmcts.reform.civil.scheduler.trialreadynotification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.TrialReadyNotificationSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrialReadyNotificationSchedulerTest {

    @Mock
    private TrialReadyNotificationSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private TrialReadyNotificationScheduledTask trialReadyNotificationScheduledTask;
    @InjectMocks
    private TrialReadyNotificationScheduler scheduler;

    @Test
    void shouldRunTrialReadyNotificationTask() {
        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(TrialReadyNotificationScheduler.SCHEDULER_NAME);
        verify(scheduledTaskRunner).run(
            eq(TrialReadyNotificationScheduler.SCHEDULER_NAME),
            any(),
            eq(trialReadyNotificationScheduledTask)
        );
    }
}
