package uk.gov.hmcts.reform.civil.scheduler.garesponsedeadline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GAResponseDeadlineSchedulerTest {

    @Mock
    private GAResponseDeadlineSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private GAResponseDeadlineTask scheduledTask;
    @InjectMocks
    private GAResponseDeadlineScheduler scheduler;

    @Test
    void shouldReturnCorrectSchedulerName() {
        assertThat(scheduler.getName()).isEqualTo(GAResponseDeadlineScheduler.SCHEDULER_NAME);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRunGAResponseDeadlineTask() {
        TaskResult<CaseDetails> expectedResult = new ListTaskResult<>(List.of());
        when(searchService.getElasticSearchResult()).thenReturn(expectedResult);

        scheduler.runScheduledTask();

        ArgumentCaptor<Supplier<TaskResult<CaseDetails>>> supplierCaptor =
            ArgumentCaptor.forClass(Supplier.class);

        verify(scheduledTaskRunner).run(
            eq(GAResponseDeadlineScheduler.SCHEDULER_NAME),
            supplierCaptor.capture(),
            eq(scheduledTask)
        );

        TaskResult<CaseDetails> actualResult = supplierCaptor.getValue().get();
        assertThat(actualResult).isSameAs(expectedResult);
    }
}
