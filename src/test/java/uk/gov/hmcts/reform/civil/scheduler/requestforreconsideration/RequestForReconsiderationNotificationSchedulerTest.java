package uk.gov.hmcts.reform.civil.scheduler.requestforreconsideration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.RequestForReconsiderationNotificationDeadlineSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestForReconsiderationNotificationSchedulerTest {

    @Mock
    private RequestForReconsiderationNotificationDeadlineSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private RequestForReconsiderationNotificationScheduledTask requestForReconsiderationNotificationScheduledTask;
    @InjectMocks
    private RequestForReconsiderationNotificationScheduler scheduler;

    @Test
    void shouldRunRequestForReconsiderationNotificationTask() {
        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(RequestForReconsiderationNotificationScheduler.SCHEDULER_NAME);
        verify(scheduledTaskRunner).run(
            eq(RequestForReconsiderationNotificationScheduler.SCHEDULER_NAME),
            any(),
            eq(requestForReconsiderationNotificationScheduledTask)
        );
    }
}
