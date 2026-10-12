package uk.gov.hmcts.reform.civil.scheduler.orderreviewobligation;

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
import uk.gov.hmcts.reform.civil.service.search.OrderReviewObligationSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderReviewObligationCheckSchedulerTest {

    @Mock
    private OrderReviewObligationSearchService searchService;

    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;

    @Mock
    private OrderReviewObligationCheckScheduledTask orderReviewObligationCheckScheduledTask;

    @Captor
    private ArgumentCaptor<ScheduledTaskConfiguration<CaseDetails, Long>> configCaptor;

    @InjectMocks
    private OrderReviewObligationCheckScheduler scheduler;

    @Test
    void shouldRunOrderReviewObligationCheckTask() {
        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(configCaptor.capture());

        ScheduledTaskConfiguration<CaseDetails, Long> config = configCaptor.getValue();
        assertThat(config.getSchedulerName()).isEqualTo(scheduler.getName());
        assertThat(config.getSearchResultSupplier().get()).isEqualTo(searchService.getElasticSearchResult());
        assertThat(config.getScheduledTask()).isEqualTo(orderReviewObligationCheckScheduledTask);
        assertThat(config.isUseDefaultInterceptors()).isFalse();
    }
}
