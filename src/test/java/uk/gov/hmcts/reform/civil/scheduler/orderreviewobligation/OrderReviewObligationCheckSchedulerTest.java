package uk.gov.hmcts.reform.civil.scheduler.orderreviewobligation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.OrderReviewObligationSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderReviewObligationCheckSchedulerTest {

    @Mock
    private OrderReviewObligationSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private OrderReviewObligationCheckScheduledTask orderReviewObligationCheckScheduledTask;
    @InjectMocks
    private OrderReviewObligationCheckScheduler scheduler;

    @Test
    void shouldRunOrderReviewObligationCheckTask() {
        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(OrderReviewObligationCheckScheduler.SCHEDULER_NAME);
        verify(scheduledTaskRunner).run(
            eq(OrderReviewObligationCheckScheduler.SCHEDULER_NAME),
            any(),
            eq(orderReviewObligationCheckScheduledTask)
        );
    }
}
