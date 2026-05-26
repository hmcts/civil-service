package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STAY_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class CheckStayOrderDeadlineEndTaskHandlerTest {

    @Mock private ExternalTask externalTask;

    @Mock private ExternalTaskService externalTaskService;

    @Mock private CaseStateSearchService searchService;

    @Mock private CaseDetailsConverter caseDetailsConverter;

    @Mock private GaCoreCaseDataService coreCaseDataService;

    private CheckStayOrderDeadlineEndTaskHandler gaOrderMadeTaskHandler;

    @Spy private ObjectMapper mapper = ObjectMapperFactory.instance();

    private CaseDetails caseDetailsWithTodayDeadlineNotProcessed;
    private CaseDetails caseDetailsWithDeadlineCrossedNotProcessed;

    private CaseDetails caseDetailsWithNoDeadline;
    private CaseDetails caseDetailsWithFutureDeadline;
    private GeneralApplicationCaseData caseDataWithDeadlineCrossedNotProcessed;
    private GeneralApplicationCaseData caseDataWithTodayDeadlineNotProcessed;
    private GeneralApplicationCaseData caseDataWithNoDeadline;
    private GeneralApplicationCaseData caseDataWithFutureDeadline;

    private final LocalDate deadlineCrossed = LocalDate.now().minusDays(2);
    private final LocalDate deadlineInFuture = LocalDate.now().plusDays(2);
    private final LocalDate deadLineToday = LocalDate.now();

    @BeforeEach
    void setUp() {
        EventProperties eventProperties = new EventProperties();
        eventProperties.setRetryCount(3);
        gaOrderMadeTaskHandler = new CheckStayOrderDeadlineEndTaskHandler(
            new ExternalTaskCompletionService(),
            eventProperties,
            searchService,
            coreCaseDataService,
            caseDetailsConverter,
            mapper
        );
    }

    @BeforeEach
    void init() {
        caseDetailsWithTodayDeadlineNotProcessed =
                getCaseDetails(1L, deadLineToday);
        caseDataWithTodayDeadlineNotProcessed =
                getCaseData(1L, deadLineToday, YesOrNo.NO);

        caseDetailsWithDeadlineCrossedNotProcessed =
                getCaseDetails(3L, deadlineCrossed);
        caseDataWithDeadlineCrossedNotProcessed =
                getCaseData(3L, deadlineCrossed, YesOrNo.NO);

        caseDetailsWithNoDeadline = getCaseDetails(5L, null);
        caseDataWithNoDeadline = getCaseData(5L, null, YesOrNo.NO);

        caseDetailsWithFutureDeadline =
                getCaseDetails(6L, deadlineInFuture);
        caseDataWithFutureDeadline = getCaseData(6L, deadlineInFuture, YesOrNo.NO);
    }

    @Test
    void shouldNotSendMessageAndTriggerGaEvent_whenZeroCasesFound() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM))
                .thenReturn(Set.of());

        gaOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM);
        verifyNoInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldNotSendMessageAndTriggerGaEvent_whenCasesPastDeadlineFoundAndDifferentAppType() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM))
                .thenReturn(Set.of());
        gaOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM);
        verifyNoInteractions(coreCaseDataService);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldNotSendMessageAndTriggerGaEvent_whenCasesHaveFutureDeadLine() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM))
                .thenReturn(Set.of(caseDetailsWithFutureDeadline));

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
        GeneralApplicationCaseData consentOrderCaseData =
                getConsentOrderCaseData(deadlineInFuture, YesOrNo.NO);
        CaseDetails consentOrderCaseDetails =
                getConsentOrderCaseDetails(deadlineInFuture);
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM))
                .thenReturn(Set.of(consentOrderCaseDetails));

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
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM))
                .thenReturn(
                        Set.of(
                                caseDetailsWithTodayDeadlineNotProcessed,
                                caseDetailsWithDeadlineCrossedNotProcessed,
                                caseDetailsWithFutureDeadline,
                                caseDetailsWithNoDeadline));

        when(caseDetailsConverter.toGeneralApplicationCaseData(
                        caseDetailsWithTodayDeadlineNotProcessed))
                .thenReturn(caseDataWithTodayDeadlineNotProcessed);

        when(caseDetailsConverter.toGeneralApplicationCaseData(
                        caseDetailsWithDeadlineCrossedNotProcessed))
                .thenReturn(caseDataWithDeadlineCrossedNotProcessed);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithFutureDeadline))
                .thenReturn(caseDataWithFutureDeadline);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithNoDeadline))
                .thenReturn(caseDataWithNoDeadline);

        gaOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM);
        verify(coreCaseDataService)
                .triggerGaEvent(
                        1L,
                        END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE,
                        getCaseData(1L, deadLineToday, YesOrNo.YES).toMap(mapper));
        verify(coreCaseDataService)
                .triggerGaEvent(
                        3L,
                        END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE,
                        getCaseData(3L, deadlineCrossed, YesOrNo.YES)
                                .toMap(mapper));
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldEmitBusinessProcessEvent_onlyWhen_NotProcessedAndDeadlineReached_consentOrder() {
        GeneralApplicationCaseData consentOrderCaseData =
                getConsentOrderCaseData(deadLineToday, YesOrNo.NO);
        CaseDetails consentOrderCaseDetails =
                getConsentOrderCaseDetails(deadLineToday);
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM))
                .thenReturn(Set.of(consentOrderCaseDetails));

        when(caseDetailsConverter.toGeneralApplicationCaseData(consentOrderCaseDetails))
                .thenReturn(consentOrderCaseData);

        gaOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM);
        verify(coreCaseDataService)
                .triggerGaEvent(
                        1L,
                        END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE,
                        getConsentOrderCaseData(deadLineToday, YesOrNo.YES)
                                .toMap(mapper));
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

    private GeneralApplicationCaseData getCaseData(Long ccdId, LocalDate deadline, YesOrNo esProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
                .ccdCaseReference(ccdId)
                .ccdState(ORDER_MADE)
                .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.STAY_THE_CLAIM)))
                .judicialDecisionMakeOrder(
                        new GAJudicialMakeAnOrder()
                                .setMakeAnOrder(APPROVE_OR_EDIT)
                                .setJudgeRecitalText("Sample Text")
                                .setJudgeApproveEditOptionDate(deadline)
                                .setReasonForDecisionText("Sample Test")
                                .setIsOrderProcessedByStayScheduler(esProcessed))
                .build();
    }

    private GeneralApplicationCaseData getConsentOrderCaseData(LocalDate deadline, YesOrNo isProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
                .ccdCaseReference(1L)
                .ccdState(ORDER_MADE)
                .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.STAY_THE_CLAIM)))
                .approveConsentOrder(
                        new GAApproveConsentOrder()
                                .setConsentOrderDescription("Testing prepopulated text")
                                .setConsentOrderDateToEnd(deadline)
                                .setIsOrderProcessedByStayScheduler(isProcessed))
                .build();
    }

    private CaseDetails getCaseDetails(Long ccdId, LocalDate deadline) {
        return CaseDetails.builder()
                .id(ccdId)
                .data(
                        Map.of(
                                "judicialDecisionMakeOrder",
                                        new GAJudicialMakeAnOrder()
                                                .setMakeAnOrder(APPROVE_OR_EDIT)
                                                .setJudgeRecitalText("Sample Text")
                                                .setJudgeApproveEditOptionDate(deadline)
                                                .setReasonForDecisionText("Sample Test")
                                                .setIsOrderProcessedByStayScheduler(YesOrNo.NO),
                                "generalAppType",
                                        new GAApplicationType()
                                                .setTypes(List.of(GeneralApplicationTypes.STAY_THE_CLAIM))))
                .state(ORDER_MADE.toString())
                .build();
    }

    private CaseDetails getConsentOrderCaseDetails(LocalDate deadline) {
        return CaseDetails.builder()
                .id(1L)
                .data(
                        Map.of(
                                "approveConsentOrder",
                                        new GAApproveConsentOrder()
                                                .setConsentOrderDescription(
                                                        "Testing prepopulated text")
                                                .setConsentOrderDateToEnd(deadline)
                                                .setIsOrderProcessedByStayScheduler(YesOrNo.NO),
                                "generalAppType",
                                        new GAApplicationType()
                                                .setTypes(List.of(GeneralApplicationTypes.STAY_THE_CLAIM))))
                .state(ORDER_MADE.toString())
                .build();
    }
}
