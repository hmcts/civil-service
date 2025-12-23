package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_SCHEDULER_CHECK_UNLESS_ORDER_DEADLINE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.RELIEF_FROM_SANCTIONS;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.UNLESS_ORDER;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    CheckUnlessOrderDeadlineEndTaskHandler.class})
public class CheckUnlessOrderDeadlineEndTaskHandlerTest {

    @MockitoBean
    private ExternalTask externalTask;

    @MockitoBean
    private ExternalTaskService externalTaskService;

    @MockitoBean
    private CaseStateSearchService searchService;

    @MockitoBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockitoBean
    private GaCoreCaseDataService coreCaseDataService;

    @Autowired
    private CheckUnlessOrderDeadlineEndTaskHandler gaUnlessOrderMadeTaskHandler;

    @Autowired
    private ObjectMapper mapper;

    private CaseDetails caseDetailsWithTodayDeadlineNotProcessed;
    private CaseDetails caseDetailsWithTodayDeadlineProcessed;
    private CaseDetails caseDetailsWithTodayDeadlineReliefFromSanctionOrder;
    private CaseDetails caseDetailsWithDeadlineCrossedNotProcessed;
    private CaseDetails caseDetailsWithDeadlineCrossedProcessed;
    private CaseDetails caseDetailsWithTodayDeadLineWithOrderProcessedNull;

    private CaseDetails caseDetailsWithNoDeadline;
    private CaseDetails caseDetailsWithFutureDeadline;
    private GeneralApplicationCaseData caseDataWithDeadlineCrossedNotProcessed;
    private GeneralApplicationCaseData caseDataWithTodayDeadlineNotProcessed;
    private GeneralApplicationCaseData caseDataWithTodayDeadlineProcessed;
    private GeneralApplicationCaseData caseDataWithTodayDeadlineReliefFromSanctionOrder;
    private GeneralApplicationCaseData caseDataWithDeadlineCrossedProcessed;
    private GeneralApplicationCaseData caseDataWithTodayDeadLineWithOrderProcessedNull;
    private GeneralApplicationCaseData caseDataWithNoDeadline;
    private GeneralApplicationCaseData caseDataWithFutureDeadline;

    private final LocalDate deadlineCrossed = LocalDate.now().minusDays(2);
    private final LocalDate deadlineInFuture = LocalDate.now().plusDays(2);
    private final LocalDate deadLineToday = LocalDate.now();

    @BeforeEach
    void init() {
        caseDetailsWithTodayDeadlineNotProcessed = getCaseDetails(1L, UNLESS_ORDER, deadLineToday,
                                                                  YesOrNo.NO);
        caseDataWithTodayDeadlineNotProcessed = getCaseData(1L, UNLESS_ORDER, deadLineToday,
                                                            YesOrNo.NO);

        caseDetailsWithTodayDeadlineProcessed = getCaseDetails(1L, UNLESS_ORDER, deadLineToday,
                                                               YesOrNo.YES);
        caseDataWithTodayDeadlineProcessed = getCaseData(1L, UNLESS_ORDER, deadLineToday,
                                                         YesOrNo.YES);

        caseDetailsWithTodayDeadlineReliefFromSanctionOrder = getCaseDetails(2L, RELIEF_FROM_SANCTIONS,
                                                                             deadLineToday, YesOrNo.NO);
        caseDataWithTodayDeadlineReliefFromSanctionOrder = getCaseData(2L, RELIEF_FROM_SANCTIONS,
                                                                       deadLineToday, YesOrNo.NO);

        caseDetailsWithDeadlineCrossedNotProcessed = getCaseDetails(3L, UNLESS_ORDER,
                                                                    deadlineCrossed, YesOrNo.NO);
        caseDataWithDeadlineCrossedNotProcessed = getCaseData(3L, UNLESS_ORDER, deadlineCrossed,
                                                              YesOrNo.NO);

        caseDetailsWithDeadlineCrossedProcessed = getCaseDetails(4L, UNLESS_ORDER, deadlineCrossed,
                                                                 YesOrNo.YES);
        caseDataWithDeadlineCrossedProcessed = getCaseData(4L, UNLESS_ORDER, deadlineCrossed,
                                                           YesOrNo.YES);

        caseDetailsWithNoDeadline = getCaseDetails(5L, UNLESS_ORDER,
                                                   null, YesOrNo.NO);
        caseDataWithNoDeadline = getCaseData(5L, UNLESS_ORDER,
                                             null, YesOrNo.NO);

        caseDetailsWithFutureDeadline = getCaseDetails(6L, UNLESS_ORDER,
                                                       deadlineInFuture, YesOrNo.NO);
        caseDataWithFutureDeadline = getCaseData(6L, UNLESS_ORDER,
                                                 deadlineInFuture, YesOrNo.NO);
        caseDetailsWithTodayDeadLineWithOrderProcessedNull = getCaseDetails(7L, UNLESS_ORDER,
                                                                            deadLineToday, null);
        caseDataWithTodayDeadLineWithOrderProcessedNull = getCaseData(7L, UNLESS_ORDER,
                                                                      deadLineToday, null);
    }

    @Test
    void shouldNotSendMessageAndTriggerGaEvent_whenZeroCasesFound() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER)).thenReturn(Set.of());

        gaUnlessOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER);
        verifyNoInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldNotSendMessageAndTriggerGaEvent_whenCasesPastDeadlineFoundAndDifferentAppType() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER)).thenReturn(Set.of(
            caseDetailsWithDeadlineCrossedProcessed
        ));

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithDeadlineCrossedProcessed))
            .thenReturn(caseDataWithDeadlineCrossedProcessed);
        gaUnlessOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER);
        verifyNoInteractions(coreCaseDataService);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldNotSendMessageAndTriggerGaEvent_whenCasesHaveFutureDeadLine() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER)).thenReturn(Set.of(
            caseDetailsWithFutureDeadline
        ));

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithFutureDeadline))
            .thenReturn(caseDataWithFutureDeadline);

        gaUnlessOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER);
        verifyNoInteractions(coreCaseDataService);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldNotTriggerBusinessProcessEventWhenIsOrderProcessedIsNull() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER)).thenReturn(
            Set.of(caseDetailsWithTodayDeadlineNotProcessed,
                   caseDetailsWithTodayDeadLineWithOrderProcessedNull));
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithTodayDeadlineNotProcessed))
            .thenReturn(caseDataWithTodayDeadlineNotProcessed);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithTodayDeadLineWithOrderProcessedNull))
            .thenReturn(caseDataWithTodayDeadLineWithOrderProcessedNull);

        gaUnlessOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER);
        verify(coreCaseDataService).triggerGaEvent(1L, END_SCHEDULER_CHECK_UNLESS_ORDER_DEADLINE,
                                                   getCaseData(1L, UNLESS_ORDER, deadLineToday,
                                                               YesOrNo.YES).toMap(mapper));
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldEmitBusinessProcessEvent_onlyWhen_NotProcessedAndDeadlineReached() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER)).thenReturn(
            Set.of(caseDetailsWithTodayDeadlineNotProcessed,
                    caseDetailsWithDeadlineCrossedNotProcessed,
                    caseDetailsWithTodayDeadlineProcessed,
                    caseDetailsWithFutureDeadline,
                    caseDetailsWithNoDeadline
            ));

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithTodayDeadlineNotProcessed))
            .thenReturn(caseDataWithTodayDeadlineNotProcessed);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithDeadlineCrossedNotProcessed))
            .thenReturn(caseDataWithDeadlineCrossedNotProcessed);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithTodayDeadlineProcessed))
            .thenReturn(caseDataWithTodayDeadlineProcessed);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithFutureDeadline))
            .thenReturn(caseDataWithFutureDeadline);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithNoDeadline))
            .thenReturn(caseDataWithNoDeadline);

        gaUnlessOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER);
        verify(coreCaseDataService).triggerGaEvent(1L, END_SCHEDULER_CHECK_UNLESS_ORDER_DEADLINE,
                                                   getCaseData(1L, UNLESS_ORDER, deadLineToday,
                                                               YesOrNo.YES).toMap(mapper));
        verify(coreCaseDataService).triggerGaEvent(3L, END_SCHEDULER_CHECK_UNLESS_ORDER_DEADLINE,
                                                   getCaseData(3L, UNLESS_ORDER, deadlineCrossed,
                                                               YesOrNo.YES).toMap(mapper));
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldEmitBusinessProcessEvent_whenCasesFoundWithNullDeadlineDate() {
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER))
            .thenReturn(Set.of(caseDetailsWithNoDeadline));

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetailsWithNoDeadline))
            .thenReturn(caseDataWithNoDeadline);

        gaUnlessOrderMadeTaskHandler.execute(externalTask, externalTaskService);

        verify(searchService).getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER);
        verifyNoInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    private GeneralApplicationCaseData getCaseData(Long ccdId, GeneralApplicationTypes generalApplicationType,
                                                   LocalDate deadline, YesOrNo isProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(ccdId)
            .ccdState(ORDER_MADE)
            .generalAppType(GAApplicationType.builder().types(List.of(generalApplicationType)).build())
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(APPROVE_OR_EDIT)
                                           .judgeRecitalText("Sample Text")
                                           .judgeApproveEditOptionDateForUnlessOrder(deadline)
                                           .reasonForDecisionText("Sample Test")
                                           .isOrderProcessedByUnlessScheduler(isProcessed)
                                           .build()).build();
    }

    private CaseDetails getCaseDetails(Long ccdId, GeneralApplicationTypes generalApplicationType,
                                       LocalDate deadline, YesOrNo isProcessed) {
        return CaseDetails.builder().id(ccdId).data(
                Map.of("judicialDecisionMakeOrder", GAJudicialMakeAnOrder.builder()
                           .makeAnOrder(APPROVE_OR_EDIT)
                           .judgeRecitalText("Sample Text")
                           .judgeApproveEditOptionDateForUnlessOrder(deadline)
                           .reasonForDecisionText("Sample Test")
                           .isOrderProcessedByUnlessScheduler(isProcessed)
                           .build(),
                       "generalAppType", GAApplicationType.builder().types(List.of(generalApplicationType)).build()))
            .state(ORDER_MADE.toString()).build();
    }
}
