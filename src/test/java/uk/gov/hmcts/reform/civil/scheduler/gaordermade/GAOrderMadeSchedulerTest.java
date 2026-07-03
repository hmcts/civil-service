package uk.gov.hmcts.reform.civil.scheduler.gaordermade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;

import java.time.LocalDate;
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
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ScheduledTaskRunner<GeneralApplicationCaseData, Long> scheduledTaskRunner;
    @Mock
    private GAOrderMadeScheduledTask gaOrderMadeScheduledTask;
    @InjectMocks
    private GAOrderMadeScheduler scheduler;

    @Test
    @SuppressWarnings("unchecked")
    void shouldRunGAOrderMadeTask() {
        CaseDetails expiredCaseDetails = CaseDetails.builder().id(1L).build();
        CaseDetails futureCaseDetails = CaseDetails.builder().id(2L).build();
        GeneralApplicationCaseData expiredCaseData = getCaseData(1L, LocalDate.now().minusDays(1));
        GeneralApplicationCaseData futureCaseData = getCaseData(2L, LocalDate.now().plusDays(1));
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM))
            .thenReturn(Set.of(expiredCaseDetails, futureCaseDetails));
        when(caseDetailsConverter.toGeneralApplicationCaseData(expiredCaseDetails)).thenReturn(expiredCaseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(futureCaseDetails)).thenReturn(futureCaseData);
        when(gaOrderMadeScheduledTask.hasExpiredStayDeadline(expiredCaseData)).thenReturn(true);
        when(gaOrderMadeScheduledTask.hasExpiredStayDeadline(futureCaseData)).thenReturn(false);

        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(GAOrderMadeScheduler.SCHEDULER_NAME);
        ArgumentCaptor<Supplier<TaskResult<GeneralApplicationCaseData>>> supplierCaptor =
            ArgumentCaptor.forClass(Supplier.class);
        verify(scheduledTaskRunner).run(
            eq(GAOrderMadeScheduler.SCHEDULER_NAME),
            supplierCaptor.capture(),
            eq(gaOrderMadeScheduledTask)
        );
        assertThat(supplierCaptor.getValue().get().itemStream()).containsExactly(expiredCaseData);
    }

    private GeneralApplicationCaseData getCaseData(Long caseId, LocalDate deadline) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(caseId)
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setJudgeApproveEditOptionDate(deadline))
            .build();
    }
}
