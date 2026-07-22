package uk.gov.hmcts.reform.civil.scheduler.hearingfee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.HearingFeeDueSearchService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HearingFeeSchedulerTest {

    @Mock
    private HearingFeeDueSearchService searchService;

    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;

    @Mock
    private HearingFeeSchedulerTask judgementBufferScheduledTask;

    @InjectMocks
    private HearingFeeScheduler scheduler;

    @Test
    void shouldRunTaskRunner_whenSchedulerIsEnabledAndFeatureToggleIsEnabled() {
        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            eq(scheduler.getName()),
            any(),
            eq(judgementBufferScheduledTask)
        );
    }
}
