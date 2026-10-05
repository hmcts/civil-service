package uk.gov.hmcts.reform.civil.scheduler.trialreadycheck;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.TrialReadyCheckSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrialReadyCheckSchedulerTest {

    @Mock
    private TrialReadyCheckSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private TrialReadyCheckScheduledTask trialReadyCheckScheduledTask;
    @InjectMocks
    private TrialReadyCheckScheduler scheduler;

    @Test
    void shouldRunTrialReadyCheckTask() {
        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(TrialReadyCheckScheduler.SCHEDULER_NAME);
        verify(scheduledTaskRunner).run(
            eq(TrialReadyCheckScheduler.SCHEDULER_NAME),
            any(),
            eq(trialReadyCheckScheduledTask)
        );
    }
}
