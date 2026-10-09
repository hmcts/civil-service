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
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.AllowedEventFlowStateCheckFactory;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.SchedulerInterceptor;
import uk.gov.hmcts.reform.civil.service.search.takecaseoffline.TakeCaseOfflineSchedulerSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;

@ExtendWith(MockitoExtension.class)
class TakeCaseOfflineSchedulerTest {

    @Mock
    private TakeCaseOfflineSchedulerSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private TakeCaseOfflineScheduledTask takeCaseOfflineScheduledTask;
    @Mock
    private AllowedEventFlowStateCheckFactory allowedEventFlowStateCheckFactory;
    @Mock
    private SchedulerInterceptor<CaseDetails> flowStateCheck;
    @InjectMocks
    private TakeCaseOfflineScheduler scheduler;

    @Test
    void shouldRunTakeCaseOfflineTask() {
        when(allowedEventFlowStateCheckFactory.forEvent(TAKE_CASE_OFFLINE)).thenReturn(flowStateCheck);
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
        assertThat(config.getInterceptors()).containsExactly(flowStateCheck);
        assertThat(config.isUseDefaultInterceptors()).isTrue();
    }
}
