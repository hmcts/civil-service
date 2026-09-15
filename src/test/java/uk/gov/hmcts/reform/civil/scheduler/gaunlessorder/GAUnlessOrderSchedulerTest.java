package uk.gov.hmcts.reform.civil.scheduler.gaunlessorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.UNLESS_ORDER;

@ExtendWith(MockitoExtension.class)
class GAUnlessOrderSchedulerTest {

    @Mock
    private CaseStateSearchService searchService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ScheduledTaskRunner<GeneralApplicationCaseData, Long> scheduledTaskRunner;
    @Mock
    private GAUnlessOrderScheduledTask gaUnlessOrderScheduledTask;
    @InjectMocks
    private GAUnlessOrderScheduler scheduler;

    @Test
    @SuppressWarnings("unchecked")
    void shouldRunGAUnlessOrderTask() {
        CaseDetails expiredCaseDetails = CaseDetails.builder().id(1L).build();
        CaseDetails futureCaseDetails = CaseDetails.builder().id(2L).build();
        GeneralApplicationCaseData expiredCaseData = getCaseData(1L, LocalDate.now().minusDays(1));
        GeneralApplicationCaseData futureCaseData = getCaseData(2L, LocalDate.now().plusDays(1));
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER))
            .thenReturn(Set.of(expiredCaseDetails, futureCaseDetails));
        when(caseDetailsConverter.toGeneralApplicationCaseData(expiredCaseDetails)).thenReturn(expiredCaseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(futureCaseDetails)).thenReturn(futureCaseData);

        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(GAUnlessOrderScheduler.SCHEDULER_NAME);
        ArgumentCaptor<Supplier<TaskResult<GeneralApplicationCaseData>>> supplierCaptor =
            ArgumentCaptor.forClass(Supplier.class);
        verify(scheduledTaskRunner).run(
            eq(GAUnlessOrderScheduler.SCHEDULER_NAME),
            supplierCaptor.capture(),
            eq(gaUnlessOrderScheduledTask)
        );
        assertThat(supplierCaptor.getValue().get().itemStream()).containsExactly(expiredCaseData);
    }

    @Test
    void shouldIdentifyExpiredUnlessOrderDeadline() {
        assertThat(scheduler.hasExpiredUnlessOrderDeadline(getCaseData(LocalDate.now(), YesOrNo.NO))).isTrue();
        assertThat(scheduler.hasExpiredUnlessOrderDeadline(getCaseData(LocalDate.now().minusDays(1), YesOrNo.NO)))
            .isTrue();
        assertThat(scheduler.hasExpiredUnlessOrderDeadline(getCaseData(LocalDate.now().plusDays(1), YesOrNo.NO)))
            .isFalse();
        assertThat(scheduler.hasExpiredUnlessOrderDeadline(getCaseData(null, YesOrNo.NO))).isFalse();
        assertThat(scheduler.hasExpiredUnlessOrderDeadline(getCaseData(LocalDate.now(), YesOrNo.YES))).isFalse();
        assertThat(scheduler.hasExpiredUnlessOrderDeadline(getCaseData(LocalDate.now(), null))).isFalse();
        assertThat(scheduler.hasExpiredUnlessOrderDeadline(GeneralApplicationCaseDataBuilder.builder()
                                                          .ccdCaseReference(1L)
                                                          .build())).isFalse();
    }

    private GeneralApplicationCaseData getCaseData(Long caseId, LocalDate deadline) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(caseId)
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setJudgeApproveEditOptionDateForUnlessOrder(deadline)
                                           .setIsOrderProcessedByUnlessScheduler(YesOrNo.NO))
            .build();
    }

    private GeneralApplicationCaseData getCaseData(LocalDate deadline, YesOrNo isProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(1L)
            .ccdState(ORDER_MADE)
            .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.UNLESS_ORDER)))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setJudgeApproveEditOptionDateForUnlessOrder(deadline)
                                           .setIsOrderProcessedByUnlessScheduler(isProcessed))
            .build();
    }
}
