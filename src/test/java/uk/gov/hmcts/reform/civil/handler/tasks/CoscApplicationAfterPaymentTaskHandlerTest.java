package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHECK_PAID_IN_FULL_SCHED_DEADLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONTACT_INFORMATION_UPDATED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_STATE;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState.ISSUED;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection.PAY_BY_DATE;

@ExtendWith(MockitoExtension.class)
public class CoscApplicationAfterPaymentTaskHandlerTest {

    private static final String CIVIL_CASE_ID = "1594901956117591";
    private static final String GENERAL_APP_CASE_ID = "1234";

    @Mock
    private ExternalTask mockExternalTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseEventTaskHandler caseEventTaskHandler;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Spy
    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(mapper);

    @InjectMocks
    private CoscApplicationAfterPaymentTaskHandler handler;

    public static final String JUDGMENT_MARK_PAID_FULL = "isJudgmentMarkedPaidInFull";
    public static final String IS_CLAIMANT_LR = "isClaimantLR";
    private final VariableMap variables = Variables.createVariables();

    @BeforeEach
    void setUp() {
        variables.putValue(FLOW_STATE, "MAIN.DRAFT");
        variables.putValue(FLOW_FLAGS, Map.of());
        variables.putValue(JUDGMENT_MARK_PAID_FULL, false);
        variables.putValue(IS_CLAIMANT_LR, false);

        when(stateFlowEngine.getStateFlow(any(CaseData.class))).thenReturn(StateFlowDTO.builder()
                                                                    .flags(Map.of())
                                                                    .stateHistory(List.of(State.from("MAIN.DRAFT")))
                                                                    .state(State.from("MAIN.DRAFT"))
                                                                    .build());
    }

    @Test
    void testStartTheEvent() {
        CaseData caseData = CaseData.builder()
            .contactDetailsUpdatedEvent(
                ContactDetailsUpdatedEvent.builder()
                    .description("Best description")
                    .summary("Even better summary")
                    .submittedByCaseworker(YES).build())
            .businessProcess(
                BusinessProcess.builder()
                    .status(BusinessProcessStatus.READY)
                    .processInstanceId("process-id").build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", CHECK_PAID_IN_FULL_SCHED_DEADLINE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, CHECK_PAID_IN_FULL_SCHED_DEADLINE))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                            .eventId(CONTACT_INFORMATION_UPDATED.name()).build());
        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class)))
            .thenReturn(caseData);

        handler.execute(mockExternalTask, externalTaskService);

        variables.putValue(JUDGMENT_MARK_PAID_FULL, false);
        variables.putValue(IS_CLAIMANT_LR, false);
        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, CHECK_PAID_IN_FULL_SCHED_DEADLINE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, variables);
    }

    @Test
    void testStartTheEventWithActiveJudgment() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(NO)
            .contactDetailsUpdatedEvent(
                ContactDetailsUpdatedEvent.builder()
                    .description("Best description")
                    .summary("Even better summary")
                    .submittedByCaseworker(YES).build())
            .businessProcess(
                BusinessProcess.builder()
                    .status(BusinessProcessStatus.READY)
                    .processInstanceId("process-id").build())
            .activeJudgment(JudgmentDetails.builder()
                                .state(ISSUED)
                                .paymentPlan(JudgmentPaymentPlan.builder().type(PAY_BY_DATE).paymentDeadlineDate(
                                    LocalDate.now()).build())
                                .orderedAmount("150001")
                                .fullyPaymentMadeDate(LocalDate.now())
                                .build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", CHECK_PAID_IN_FULL_SCHED_DEADLINE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, CHECK_PAID_IN_FULL_SCHED_DEADLINE))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                            .eventId(CONTACT_INFORMATION_UPDATED.name()).build());
        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class)))
            .thenReturn(caseData);

        handler.execute(mockExternalTask, externalTaskService);
        variables.putValue(JUDGMENT_MARK_PAID_FULL, true);
        variables.putValue(IS_CLAIMANT_LR, false);
        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, CHECK_PAID_IN_FULL_SCHED_DEADLINE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, variables);
    }

    @Test
    void testStartTheEventWithActiveJudgmentClaimantLR() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YES)
            .contactDetailsUpdatedEvent(
                ContactDetailsUpdatedEvent.builder()
                    .description("Best description")
                    .summary("Even better summary")
                    .submittedByCaseworker(YES).build())
            .businessProcess(
                BusinessProcess.builder()
                    .status(BusinessProcessStatus.READY)
                    .processInstanceId("process-id").build())
            .activeJudgment(JudgmentDetails.builder()
                                .state(ISSUED)
                                .paymentPlan(JudgmentPaymentPlan.builder().type(PAY_BY_DATE).paymentDeadlineDate(
                                    LocalDate.now()).build())
                                .orderedAmount("150001")
                                .fullyPaymentMadeDate(LocalDate.now())
                                .build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", CHECK_PAID_IN_FULL_SCHED_DEADLINE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, CHECK_PAID_IN_FULL_SCHED_DEADLINE))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                            .eventId(CONTACT_INFORMATION_UPDATED.name()).build());
        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class)))
            .thenReturn(caseData);

        handler.execute(mockExternalTask, externalTaskService);
        variables.putValue(JUDGMENT_MARK_PAID_FULL, true);
        variables.putValue(IS_CLAIMANT_LR, true);
        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, CHECK_PAID_IN_FULL_SCHED_DEADLINE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, variables);
    }
}

