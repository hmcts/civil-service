package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_PBA_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_STATE;

@ExtendWith(MockitoExtension.class)
class PaymentTaskHandlerTest {

    private static final String CASE_ID = "1";

    @Mock
    private ExternalTask mockExternalTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
    @InjectMocks
    private PaymentTaskHandler paymentTaskHandler;

    @BeforeEach
    void init() {

    }

    @Nested
    class SuccessHandler {

        @Test
        void shouldTriggerMakePbaPaymentCCDEvent_whenHandlerIsExecuted() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();
            VariableMap variables = Variables.createVariables();
            variables.putValue(FLOW_STATE, "MAIN.CLAIM_SUBMITTED");
            variables.putValue(FLOW_FLAGS, getVariableMap());

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(mockExternalTask.getTopicName()).thenReturn("test");
            when(mockExternalTask.getActivityId()).thenReturn("activityId");
            when(mockExternalTask.getAllVariables())
                .thenReturn(Map.of("caseId", CASE_ID, "caseEvent", MAKE_PBA_PAYMENT.name()));

            when(stateFlowEngine.getStateFlow(any(CaseData.class))).thenReturn(getStateFlowDTO());

            when(coreCaseDataService.startUpdate(CASE_ID, MAKE_PBA_PAYMENT))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());
            when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

            paymentTaskHandler.execute(mockExternalTask, externalTaskService);

            verify(coreCaseDataService).startUpdate(CASE_ID, MAKE_PBA_PAYMENT);
            verify(coreCaseDataService).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
            verify(externalTaskService).complete(mockExternalTask, variables);
        }
    }

    @Nested
    class NotRetryableFailureTest {
        @Test
        void shouldNotCallHandleFailureMethod_whenValueMapperExceptionThrown() {
            //given: ExternalTask.getAllVariables throws ValueMapperException
            when(mockExternalTask.getAllVariables())
                .thenThrow(new ValueMapperException("Mapper conversion failed due to incompatible types"));

            //Task handler is called and ValueMapperException is thrown
            paymentTaskHandler.execute(mockExternalTask, externalTaskService);

            //then: Retry should not happen in this case
            verify(externalTaskService).handleFailure(
                any(ExternalTask.class),
                anyString(),
                anyString(),
                anyInt(),
                anyLong()
            );
        }

        @Test
        void shouldNotCallHandleFailureMethod_whenIllegalArgumentExceptionThrown() {
            //given: ExternalTask variables with incompatible event type
            String incompatibleEventType = "test";
            Map<String, Object> allVariables = Map.of("caseId", CASE_ID, "caseEvent", incompatibleEventType);
            when(mockExternalTask.getAllVariables())
                .thenReturn(allVariables);

            //when: Task handler is called and IllegalArgumentException is thrown
            paymentTaskHandler.execute(mockExternalTask, externalTaskService);

            //then: Retry should not happen in this case
            verify(externalTaskService).handleFailure(
                any(ExternalTask.class),
                anyString(),
                anyString(),
                anyInt(),
                anyLong()
            );
        }

        @Test
        void shouldNotCallHandleFailureMethod_whenCaseIdNotFound() {
            //given: ExternalTask variables without caseId
            Map<String, Object> allVariables = Map.of("caseEvent", NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE);
            when(mockExternalTask.getAllVariables())
                .thenReturn(allVariables);

            //when: Task handler is called and CaseIdNotProvidedException is thrown
            paymentTaskHandler.execute(mockExternalTask, externalTaskService);

            //then: Retry should not happen in this case
            verify(externalTaskService).handleFailure(
                any(ExternalTask.class),
                anyString(),
                anyString(),
                anyInt(),
                anyLong()
            );
        }
    }

    private StateFlowDTO getStateFlowDTO() {
        return StateFlowDTO.builder()
            .state(State.from("MAIN.CLAIM_SUBMITTED"))
            .flags(getVariableMap())
            .build();
    }

    private Map<String, Boolean> getVariableMap() {
        return Map.ofEntries(
            Map.entry("ONE_RESPONDENT_REPRESENTATIVE", true),
            Map.entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
            Map.entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), false),
            Map.entry(FlowFlag.CASE_PROGRESSION_ENABLED.name(), false),
            Map.entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            Map.entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), false),
            Map.entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            Map.entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), false),
            Map.entry(FlowFlag.CLAIM_STATE_DURING_NOC.name(), false),
            Map.entry(FlowFlag.WELSH_ENABLED.name(), false)
        );
    }
}
