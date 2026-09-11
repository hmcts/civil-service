package uk.gov.hmcts.reform.civil.scheduler.gaordermade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;

import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STAY_THE_CLAIM;

@ExtendWith(MockitoExtension.class)
class GAOrderMadeSchedulerTest {

    @Mock
    private CaseStateSearchService searchService;
    @Mock
    private GAOrderMadeStayDeadlineFilter filter;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private GAOrderMadeScheduledTask gaOrderMadeScheduledTask;
    @InjectMocks
    private GAOrderMadeScheduler scheduler;

    @Test
    @SuppressWarnings("unchecked")
    void shouldRunGAOrderMadeTask() {
        CaseDetails expiredCaseDetails = CaseDetails.builder().id(1L).build();
        CaseDetails futureCaseDetails = CaseDetails.builder().id(2L).build();
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM))
            .thenReturn(Set.of(expiredCaseDetails, futureCaseDetails));
        when(filter.hasExpiredStayDeadline(expiredCaseDetails)).thenReturn(true);
        when(filter.hasExpiredStayDeadline(futureCaseDetails)).thenReturn(false);

        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(GAOrderMadeScheduler.SCHEDULER_NAME);
        ArgumentCaptor<Supplier<TaskResult<CaseDetails>>> supplierCaptor =
            ArgumentCaptor.forClass(Supplier.class);
        verify(scheduledTaskRunner).run(
            eq(GAOrderMadeScheduler.SCHEDULER_NAME),
            supplierCaptor.capture(),
            eq(gaOrderMadeScheduledTask)
        );
        TaskResult<CaseDetails> result = supplierCaptor.getValue().get();
        assertThat(result).isInstanceOf(ListTaskResult.class);
        assertThat(result.itemStream()).containsExactly(expiredCaseDetails);
    }
}
