package uk.gov.hmcts.reform.civil.scheduler.settlementnoresponsefromdefchk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;
import uk.gov.hmcts.reform.civil.service.search.SettlementNoResponseFromDefendantSearchService;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SettlementNoResponseFromDefendantCheckSchedulerTest {

    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;

    @Mock
    private SettlementNoResponseFromDefendantSearchService searchService;

    @Mock
    private SettlementNoResponseFromDefendantCheckScheduledTask scheduledTask;

    private SettlementNoResponseFromDefendantCheckScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SettlementNoResponseFromDefendantCheckScheduler(
            scheduledTaskRunner,
            searchService,
            scheduledTask
        );
    }

    @Test
    void shouldReturnSchedulerName() {
        assertThat(scheduler.getName())
            .isEqualTo(SettlementNoResponseFromDefendantCheckScheduler.SCHEDULER_NAME);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRunScheduledTask() {
        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            eq(SettlementNoResponseFromDefendantCheckScheduler.SCHEDULER_NAME),
            ArgumentMatchers.<Supplier<? extends TaskResult<CaseDetails>>>any(),
            eq(scheduledTask)
        );
    }
}
