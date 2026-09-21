package uk.gov.hmcts.reform.civil.scheduler.decisionoutcome;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.DecisionOutcomeSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DecisionOutcomeSchedulerTest {

    @Mock
    private DecisionOutcomeSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private DecisionOutcomeScheduledTask decisionOutcomeScheduledTask;
    @InjectMocks
    private DecisionOutcomeScheduler scheduler;

    @Test
    void shouldRunDecisionOutcomeTask() {
        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(DecisionOutcomeScheduler.SCHEDULER_NAME);
        verify(scheduledTaskRunner).run(
            eq(DecisionOutcomeScheduler.SCHEDULER_NAME),
            any(),
            eq(decisionOutcomeScheduledTask)
        );
    }
}
