package uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.CaseReadyBusinessProcessSearchService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PollingEventEmitterSchedulerTest {

    private static final String SCHEDULER_NAME = "PollingEventEmitter";

    @Mock
    private CaseReadyBusinessProcessSearchService searchService;

    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;

    @Mock
    private PollingEventEmitterScheduledTask pollingEventEmitterScheduledTask;

    @InjectMocks
    private PollingEventEmitterScheduler scheduler;

    @Test
    void shouldRunScheduledTaskRunner() {
        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            eq(SCHEDULER_NAME),
            any(),
            eq(pollingEventEmitterScheduledTask)
        );
    }
}
