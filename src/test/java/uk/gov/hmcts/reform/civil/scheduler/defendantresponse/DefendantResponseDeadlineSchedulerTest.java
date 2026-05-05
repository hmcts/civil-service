package uk.gov.hmcts.reform.civil.scheduler.defendantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.DefendantResponseDeadlineCheckSearchService;

import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.scheduler.defendantresponse.DefendantResponseDeadlineScheduler.SCHEDULER_NAME;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDeadlineSchedulerTest {

    @Mock
    private DefendantResponseDeadlineCheckSearchService searchService;

    @Mock
    private ScheduledTaskRunner scheduledTaskRunner;

    @Mock
    private DefendantResponseDeadlineTask defendantResponseDeadlineTask;

    @InjectMocks
    private DefendantResponseDeadlineScheduler scheduler;

    @SuppressWarnings("unchecked")
    @Test
    void shouldRunTaskRunner_whenDeadlineCheckIsCalled() {
        ScheduledTaskEventConfiguration expectedConfig = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);

        scheduler.deadlineCheck();

        verify(scheduledTaskRunner).run(
            eq(expectedConfig),
            any(Supplier.class),
            eq(defendantResponseDeadlineTask)
        );
    }
}
