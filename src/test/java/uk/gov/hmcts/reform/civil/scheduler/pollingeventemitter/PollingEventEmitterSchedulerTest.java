package uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.CaseReadyBusinessProcessSearchService;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Captor
    private ArgumentCaptor<ScheduledTaskConfiguration<CaseDetails, Long>> configCaptor;

    @InjectMocks
    private PollingEventEmitterScheduler scheduler;

    @Test
    void shouldRunScheduledTaskRunner() {
        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(configCaptor.capture());

        ScheduledTaskConfiguration<CaseDetails, Long> config = configCaptor.getValue();
        assertThat(config.getSchedulerName()).isEqualTo(SCHEDULER_NAME);
        assertThat(config.getScheduledTask()).isEqualTo(pollingEventEmitterScheduledTask);
        assertThat(config.isUseDefaultInterceptors()).isFalse();
    }
}
