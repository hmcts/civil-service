package uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.UnnotifiedHearingsSearchService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AutomatedHearingNoticeSchedulerTest {

    private static final String SCHEDULER_NAME = "AutomatedHearingNotice";

    @Mock
    private UnnotifiedHearingsSearchService searchService;
    @Mock
    private ScheduledTaskRunner<String, String> scheduledTaskRunner;
    @Mock
    private AutomatedHearingNoticeScheduledTask scheduledTask;

    @InjectMocks
    private AutomatedHearingNoticeScheduler scheduler;

    @Test
    void shouldRunScheduledTaskRunner() {
        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            eq(SCHEDULER_NAME),
            any(),
            eq(scheduledTask)
        );
    }
}
