package uk.gov.hmcts.reform.civil.scheduler.defendantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.DefendantResponseDeadlineCheckSearchService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDeadlineSchedulerTest {

    private static final String SCHEDULER_NAME = "DefendantResponseDeadline";

    @Mock
    private DefendantResponseDeadlineCheckSearchService searchService;

    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;

    @Mock
    private DefendantResponseDeadlineTask defendantResponseDeadlineTask;

    @InjectMocks
    private DefendantResponseDeadlineScheduler scheduler;

    @Test
    void shouldRunScheduledTaskRunner() {
        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            eq(SCHEDULER_NAME),
            any(),
            eq(defendantResponseDeadlineTask)
        );
    }
}
