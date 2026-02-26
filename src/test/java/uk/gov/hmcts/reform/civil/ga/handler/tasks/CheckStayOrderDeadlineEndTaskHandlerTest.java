package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.RELIEF_FROM_SANCTIONS;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STAY_THE_CLAIM;

@ExtendWith(MockitoExtension.class)
class CheckStayOrderDeadlineEndTaskHandlerTest {

    @Mock
    private ExternalTask externalTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private CaseStateSearchService searchService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;

    @InjectMocks
    private CheckStayOrderDeadlineEndTaskHandler gaOrderMadeTaskHandler;

    @Spy
    private ObjectMapper mapper = ObjectMapperFactory.instance();

    private CaseDetails caseDetailsWithTodayDeadlineNotProcessed;
    private CaseDetails caseDetailsWithTodayDeadlineReliefFromSanctionOrder;
    private CaseDetails caseDetailsWithDeadlineCrossedNotProcessed;
    private CaseDetails caseDetailsWithDeadlineCrossedProcessed;

    private CaseDetails caseDetailsWithNoDeadline;
    private CaseDetails caseDetailsWithFutureDeadline;
    private GeneralApplicationCaseData caseDataWithDeadlineCrossedNotProcessed;
    private GeneralApplicationCaseData caseDataWithTodayDeadlineNotProcessed;
    private GeneralApplicationCaseData caseDataWithTodayDeadlineReliefFromSanctionOrder;
    private GeneralApplicationCaseData caseDataWithDeadlineCrossedProcessed;
    private GeneralApplicationCaseData caseDataWithNoDeadline;
    private GeneralApplicationCaseData caseDataWithFutureDeadline;

    private final LocalDate deadlineCrossed = LocalDate.now().minusDays(2);
    private final LocalDate deadlineInFuture = LocalDate.now().plusDays(2);
    private final LocalDate deadLineToday = LocalDate.now();

    @BeforeEach
    void init() {
        caseDetailsWithTodayDeadlineNotProcessed = getCaseDetails(1L, STAY_THE_CLAIM, deadLineToday,
                                                                  YesOrNo.NO);
        caseDataWithTodayDeadlineNotProcessed = getCaseData(1L, STAY_THE_CLAIM, deadLineToday,
                                                            YesOrNo.NO);

        caseDetailsWithTodayDeadlineReliefFromSanctionOrder = getCaseDetails(2L, RELIEF_FROM_SANCTIONS,
                                                                             deadLineToday, YesOrNo.NO);
        caseDataWithTodayDeadlineReliefFromSanctionOrder = getCaseData(2L, RELIEF_FROM_SANCTIONS,
                                                            deadLineToday, YesOrNo.NO);

        caseDetailsWithDeadlineCrossedNotProcessed = getCaseDetails(3L, STAY_THE_CLAIM,
                                                                    deadlineCrossed, YesOrNo.NO);
        caseDataWithDeadlineCrossedNotProcessed = getCaseData(3L, STAY_THE_CLAIM, deadlineCrossed,
                                                              YesOrNo.NO);

        caseDetailsWithDeadlineCrossedProcessed = getCaseDetails(4L, STAY_THE_CLAIM, deadlineCrossed,
                                                                 YesOrNo.YES);
        caseDataWithDeadlineCrossedProcessed = getCaseData(4L, STAY_THE_CLAIM, deadlineCrossed,
                                                           YesOrNo.YES);

        caseDetailsWithNoDeadline = getCaseDetails(5L, STAY_THE_CLAIM,
                                                   null, YesOrNo.NO);
        caseDataWithNoDeadline = getCaseData(5L, STAY_THE_CLAIM,
                                                            null, YesOrNo.NO);

        caseDetailsWithFutureDeadline = getCaseDetails(6L, STAY_THE_CLAIM,
                                                       deadlineInFuture, YesOrNo.NO);
        caseDataWithFutureDeadline = getCaseData(6L, STAY_THE_CLAIM,
                                                 deadlineInFuture, YesOrNo.NO);
    }

    @Test
    void shouldNotSendMessageAndTriggerGaEvent_whenZeroCasesFound() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM)).thenReturn(Set.of());

        gaOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM);
        verifyNoInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldNotSendMessageAndTriggerGaEvent_whenCasesPastDeadlineFoundAndDifferentAppType() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM)).thenReturn(Set.of());
        gaOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM);
        verifyNoInteractions(coreCaseDataService);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldNotSendMessageAndTriggerGaEvent_whenCasesHaveFutureDeadLine() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM)).thenReturn(Set.of(
            caseDetailsWithFutureDeadline
        ));

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithFutureDeadline))
            .thenReturn(caseDataWithFutureDeadline);

        gaOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM);
        verifyNoInteractions(coreCaseDataService);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldNotSendMessageAndTriggerGaEvent_whenCasesHaveFutureDeadLine_consentOrder() {
        GeneralApplicationCaseData consentOrderCaseData = getConsentOrderCaseData(1L, STAY_THE_CLAIM, deadlineInFuture,
                                                                                  YesOrNo.NO);
        CaseDetails consentOrderCaseDetails = getConsentOrderCaseDetails(1L, STAY_THE_CLAIM, deadlineInFuture,
                                                                         YesOrNo.NO);
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM)).thenReturn(
            Set.of(consentOrderCaseDetails));

        when(caseDetailsConverter.toGeneralApplicationCaseData(consentOrderCaseDetails))
            .thenReturn(consentOrderCaseData);

        gaOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM);
        verifyNoInteractions(coreCaseDataService);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldEmitBusinessProcessEvent_onlyWhen_NotProcessedAndDeadlineReached() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM)).thenReturn(
            Set.of(caseDetailsWithTodayDeadlineNotProcessed,
                caseDetailsWithDeadlineCrossedNotProcessed,
                caseDetailsWithFutureDeadline,
                caseDetailsWithNoDeadline
        ));

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithTodayDeadlineNotProcessed))
            .thenReturn(caseDataWithTodayDeadlineNotProcessed);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithDeadlineCrossedNotProcessed))
            .thenReturn(caseDataWithDeadlineCrossedNotProcessed);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithFutureDeadline))
            .thenReturn(caseDataWithFutureDeadline);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithNoDeadline))
            .thenReturn(caseDataWithNoDeadline);

        gaOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM);
        verify(coreCaseDataService).triggerGaEvent(1L, END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE,
                                                   getCaseData(1L, STAY_THE_CLAIM, deadLineToday,
                                                               YesOrNo.YES).toMap(mapper));
        verify(coreCaseDataService).triggerGaEvent(3L, END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE,
                                                   getCaseData(3L, STAY_THE_CLAIM, deadlineCrossed,
                                                               YesOrNo.YES).toMap(mapper));
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldEmitBusinessProcessEvent_onlyWhen_NotProcessedAndDeadlineReached_consentOrder() {
        GeneralApplicationCaseData consentOrderCaseData = getConsentOrderCaseData(1L, STAY_THE_CLAIM, deadLineToday,
                                                                YesOrNo.NO);
        CaseDetails consentOrderCaseDetails = getConsentOrderCaseDetails(1L, STAY_THE_CLAIM, deadLineToday,
                                                             YesOrNo.NO);
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM)).thenReturn(
            Set.of(consentOrderCaseDetails));

        when(caseDetailsConverter.toGeneralApplicationCaseData(consentOrderCaseDetails))
            .thenReturn(consentOrderCaseData);

        gaOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM);
        verify(coreCaseDataService).triggerGaEvent(1L, END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE,
                                                   getConsentOrderCaseData(1L, STAY_THE_CLAIM, deadLineToday,
                                                                           YesOrNo.YES).toMap(mapper));
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldEmitBusinessProcessEvent_whenCasesFoundWithNullDeadlineDate() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM))
            .thenReturn(Set.of(caseDetailsWithNoDeadline));

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithNoDeadline))
            .thenReturn(caseDataWithNoDeadline);

        gaOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM);
        verifyNoInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    private GeneralApplicationCaseData getCaseData(Long ccdId, GeneralApplicationTypes generalApplicationType,
                                 LocalDate deadline, YesOrNo esProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(ccdId)
            .ccdState(ORDER_MADE)
            .generalAppType(GAApplicationType.builder().types(List.of(generalApplicationType)).build())
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setMakeAnOrder(APPROVE_OR_EDIT)
                                           .setJudgeRecitalText("Sample Text")
                                           .setJudgeApproveEditOptionDate(deadline)
                                           .setReasonForDecisionText("Sample Test")
                                           .setIsOrderProcessedByStayScheduler(esProcessed)).build();
    }

    private GeneralApplicationCaseData getConsentOrderCaseData(Long ccdId, GeneralApplicationTypes generalApplicationType,
                                 LocalDate deadline, YesOrNo isProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(ccdId)
            .ccdState(ORDER_MADE)
            .generalAppType(GAApplicationType.builder().types(List.of(generalApplicationType)).build())
            .approveConsentOrder(new GAApproveConsentOrder()
                                     .setConsentOrderDescription("Testing prepopulated text")
                                     .setConsentOrderDateToEnd(deadline)
                                     .setIsOrderProcessedByStayScheduler(isProcessed)
                                     )
            .build();
    }

    private CaseDetails getCaseDetails(Long ccdId, GeneralApplicationTypes generalApplicationType,
                                 LocalDate deadline, YesOrNo isProcessed) {
        return CaseDetails.builder().id(ccdId).data(
                Map.of("judicialDecisionMakeOrder", new GAJudicialMakeAnOrder()
                           .setMakeAnOrder(APPROVE_OR_EDIT)
                           .setJudgeRecitalText("Sample Text")
                           .setJudgeApproveEditOptionDate(deadline)
                           .setReasonForDecisionText("Sample Test")
                           .setIsOrderProcessedByStayScheduler(isProcessed),
                       "generalAppType", GAApplicationType.builder().types(List.of(generalApplicationType))
                           .build()))
            .state(ORDER_MADE.toString()).build();
    }

    private CaseDetails getConsentOrderCaseDetails(Long ccdId, GeneralApplicationTypes generalApplicationType,
                                       LocalDate deadline, YesOrNo isProcessed) {
        return CaseDetails.builder().id(ccdId).data(
                Map.of("approveConsentOrder", new GAApproveConsentOrder()
                           .setConsentOrderDescription("Testing prepopulated text")
                           .setConsentOrderDateToEnd(deadline)
                           .setIsOrderProcessedByStayScheduler(isProcessed),
                       "generalAppType", GAApplicationType.builder().types(List.of(generalApplicationType))
                           .build()))
            .state(ORDER_MADE.toString()).build();
    }
}
