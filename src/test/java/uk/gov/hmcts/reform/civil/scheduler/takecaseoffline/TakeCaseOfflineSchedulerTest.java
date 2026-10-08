package uk.gov.hmcts.reform.civil.scheduler.takecaseoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.takecaseoffline.TakeCaseOfflineSchedulerSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TakeCaseOfflineSchedulerTest {

    @Mock
    private TakeCaseOfflineSchedulerSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private TakeCaseOfflineScheduledTask takeCaseOfflineScheduledTask;
    @Mock
    private TakeCaseOfflineFlowStateInterceptor takeCaseOfflineFlowStateInterceptor;
    @InjectMocks
    private TakeCaseOfflineScheduler scheduler;

    @Test
    void shouldRunTakeCaseOfflineTask() {
        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo("TakeCaseOffline");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ScheduledTaskConfiguration<CaseDetails, Long>> captor =
            ArgumentCaptor.forClass(ScheduledTaskConfiguration.class);
        verify(scheduledTaskRunner).run(captor.capture());

        ScheduledTaskConfiguration<CaseDetails, Long> config = captor.getValue();
        assertThat(config.getSchedulerName()).isEqualTo(TakeCaseOfflineScheduler.SCHEDULER_NAME);
        assertThat(config.getScheduledTask()).isSameAs(takeCaseOfflineScheduledTask);
        assertThat(config.getSearchResultSupplier()).isNotNull();
        assertThat(config.getInterceptors()).containsExactly(takeCaseOfflineFlowStateInterceptor);
        assertThat(config.isUseDefaultInterceptors()).isTrue();
    }
}
