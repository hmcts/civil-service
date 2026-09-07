package uk.gov.hmcts.reform.civil.scheduler.gaordermade;

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
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
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

    @Test
    void shouldIdentifyExpiredStayDeadline() {
        assertThat(scheduler.hasExpiredStayDeadline(getJudicialOrderCaseData(LocalDate.now(), YesOrNo.NO))).isTrue();
        assertThat(scheduler.hasExpiredStayDeadline(getJudicialOrderCaseData(LocalDate.now().minusDays(1), YesOrNo.NO)))
            .isTrue();
        assertThat(scheduler.hasExpiredStayDeadline(getConsentOrderCaseData(LocalDate.now(), YesOrNo.NO))).isTrue();
        assertThat(scheduler.hasExpiredStayDeadline(getJudicialOrderCaseData(LocalDate.now().plusDays(1), YesOrNo.NO)))
            .isFalse();
        assertThat(scheduler.hasExpiredStayDeadline(getJudicialOrderCaseData(null, YesOrNo.NO))).isFalse();
    }

    private GeneralApplicationCaseData getCaseData(Long caseId, LocalDate deadline) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(caseId)
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setJudgeApproveEditOptionDate(deadline))
            .build();
    }

    private GeneralApplicationCaseData getJudicialOrderCaseData(LocalDate deadline, YesOrNo isProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(1L)
            .ccdState(ORDER_MADE)
            .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.STAY_THE_CLAIM)))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setJudgeApproveEditOptionDate(deadline)
                                           .setIsOrderProcessedByStayScheduler(isProcessed))
            .build();
    }

    private GeneralApplicationCaseData getConsentOrderCaseData(LocalDate deadline, YesOrNo isProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(1L)
            .ccdState(ORDER_MADE)
            .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.STAY_THE_CLAIM)))
            .approveConsentOrder(new GAApproveConsentOrder()
                                     .setConsentOrderDescription("Testing prepopulated text")
                                     .setConsentOrderDateToEnd(deadline)
                                     .setIsOrderProcessedByStayScheduler(isProcessed))
            .build();
    }
}
