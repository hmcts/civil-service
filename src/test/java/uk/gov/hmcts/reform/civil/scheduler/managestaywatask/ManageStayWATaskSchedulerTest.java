package uk.gov.hmcts.reform.civil.scheduler.managestaywatask;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.ManageStayUpdateRequestedSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManageStayWATaskSchedulerTest {

    @Mock
    private ManageStayUpdateRequestedSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private ManageStayWATaskScheduledTask manageStayWATaskScheduledTask;
    @InjectMocks
    private ManageStayWATaskScheduler scheduler;

    @Test
    void shouldRunManageStayWATask() {
        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(ManageStayWATaskScheduler.SCHEDULER_NAME);
        verify(scheduledTaskRunner).run(
            eq(ManageStayWATaskScheduler.SCHEDULER_NAME),
            any(),
            eq(manageStayWATaskScheduledTask)
        );
    }
}
