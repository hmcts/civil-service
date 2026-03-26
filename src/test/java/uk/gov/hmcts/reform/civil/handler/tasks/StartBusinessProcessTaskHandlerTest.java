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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
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
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.START_BUSINESS_PROCESS;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.FINISHED;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.STARTED;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_STATE;
import static uk.gov.hmcts.reform.civil.handler.tasks.StartBusinessProcessTaskHandler.BUSINESS_PROCESS;

@ExtendWith(MockitoExtension.class)
class StartBusinessProcessTaskHandlerTest {

    private static final String CASE_ID = "1";
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";
    public static final String ERROR_CODE = "ABORT";

    @Mock
    private ExternalTask mockTask;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
    @InjectMocks
    private StartBusinessProcessTaskHandler handler;

    private final VariableMap variables = Variables.createVariables();

    @BeforeEach
    void init() {
        variables.putValue(FLOW_STATE, "MAIN.DRAFT");
        variables.putValue(FLOW_FLAGS, Map.of());

        when(mockTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);

        when(mockTask.getAllVariables()).thenReturn(Map.of(
            "caseId", CASE_ID,
            "caseEvent", START_BUSINESS_PROCESS.name()
        ));
    }

    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, names = {"READY", "DISPATCHED"})
    void shouldStartBusinessProcess_whenValidBusinessProcessStatus(BusinessProcessStatus status) {
        BusinessProcess businessProcess = new BusinessProcess().setStatus(status);
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startUpdate(CASE_ID, START_BUSINESS_PROCESS)).thenReturn(startEventResponse);
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);
        when(mockTask.getTopicName()).thenReturn("test");
        when(stateFlowEngine.getStateFlow(any(CaseData.class))).thenReturn(new StateFlowDTO()
            .setState(State.from("MAIN.DRAFT"))
            .setFlags(Map.of()));

        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_BUSINESS_PROCESS);
        verify(coreCaseDataService).submitUpdate(CASE_ID, content(startEventResponse, businessProcess.start()));
        verify(externalTaskService).complete(mockTask, variables);
    }

    @Test
    void shouldNotUpdateBusinessProcess_whenInputStatusIsStartedAndHaveDifferentProcessInstanceId() {
        BusinessProcess businessProcess = getBusinessProcess(STARTED, "differentId");
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startUpdate(CASE_ID, START_BUSINESS_PROCESS)).thenReturn(startEventResponse);
        //when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);
        when(mockTask.getTopicName()).thenReturn("test");
        //when(mockTask.getActivityId()).thenReturn("activityId");

        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_BUSINESS_PROCESS);
        verify(externalTaskService, never()).complete(any(), any());
        verify(coreCaseDataService, never()).submitUpdate(anyString(), any(CaseDataContent.class));
        verify(externalTaskService).handleBpmnError(mockTask, ERROR_CODE);
    }

    @Test
    void shouldCompleteWithoutUpdate_whenBusinessProcessStatusIsStartedAndProcessInstanceMatches() {
        BusinessProcess businessProcess = getBusinessProcess(STARTED, PROCESS_INSTANCE_ID);
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startUpdate(CASE_ID, START_BUSINESS_PROCESS)).thenReturn(startEventResponse);
        when(mockTask.getTopicName()).thenReturn("test");
        when(stateFlowEngine.getStateFlow(any(CaseData.class))).thenReturn(new StateFlowDTO()
            .setState(State.from("MAIN.DRAFT"))
            .setFlags(Map.of()));

        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_BUSINESS_PROCESS);
        verify(coreCaseDataService, never()).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
        verify(externalTaskService, never()).handleBpmnError(mockTask, ERROR_CODE);
        verify(externalTaskService).complete(mockTask, variables);
    }

    @Test
    void shouldRaiseBpmnError_whenBusinessProcessStatusIsFinished() {
        BusinessProcess businessProcess = getBusinessProcess(FINISHED, PROCESS_INSTANCE_ID);
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startUpdate(CASE_ID, START_BUSINESS_PROCESS)).thenReturn(startEventResponse);

        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_BUSINESS_PROCESS);
        verify(coreCaseDataService, never()).submitUpdate(anyString(), any(CaseDataContent.class));
        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
        verify(externalTaskService).handleBpmnError(mockTask, ERROR_CODE);
    }

    private CaseDataContent content(StartEventResponse startEventResponse, BusinessProcess businessProcess) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.put(BUSINESS_PROCESS, businessProcess);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(data)
            .build();
    }

    private BusinessProcess getBusinessProcess(BusinessProcessStatus started, String processInstanceId) {
        return new BusinessProcess()
            .setStatus(started)
            .setProcessInstanceId(processInstanceId);
    }
}
