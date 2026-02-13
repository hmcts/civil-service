package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.ga.stateflow.GaStateFlow;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.START_GA_BUSINESS_PROCESS;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.FINISHED;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.STARTED;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;
import static uk.gov.hmcts.reform.civil.ga.handler.tasks.GaStartGeneralApplicationBusinessProcessTaskHandler.BUSINESS_PROCESS;
import static uk.gov.hmcts.reform.civil.ga.handler.tasks.GaStartGeneralApplicationBusinessProcessTaskHandler.FLOW_STATE;

@ExtendWith(MockitoExtension.class)
class GaStartGeneralApplicationBusinessProcessTaskHandlerTest {

    private static final String CASE_ID = "1";
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";
    public static final String ERROR_CODE = "ABORT";

    @Mock
    private ExternalTask mockTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private GaCoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private GaStateFlowEngine gaStateFlowEngine;
    @InjectMocks
    private GaStartGeneralApplicationBusinessProcessTaskHandler handler;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private final VariableMap variables = Variables.createVariables();

    @BeforeEach
    void init() {
        variables.putValue(FLOW_STATE, "MAIN.DRAFT");
        variables.putValue(FLOW_FLAGS, Map.of());
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);

        when(mockTask.getAllVariables()).thenReturn(Map.of(
            "caseId", CASE_ID,
            "caseEvent", START_GA_BUSINESS_PROCESS.name()
        ));
    }

    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, names = {"READY", "DISPATCHED"})
    void shouldStartBusinessProcess_whenValidBusinessProcessStatus(BusinessProcessStatus status) {
        GaStateFlow stateFlow = mock(GaStateFlow.class);
        State state = mock(State.class);
        when(state.getName()).thenReturn("MAIN.DRAFT");
        when(stateFlow.getState()).thenReturn(state);
        when(stateFlow.getFlags()).thenReturn(Map.of());
        when(gaStateFlowEngine.evaluate(any(GeneralApplicationCaseData.class))).thenReturn(stateFlow);

        BusinessProcess businessProcess = new BusinessProcess().setStatus(status);
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft().businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(any(CaseDetails.class))).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(CASE_ID, START_GA_BUSINESS_PROCESS))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.submitGaUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_GA_BUSINESS_PROCESS);
        verify(coreCaseDataService).submitGaUpdate(CASE_ID, content(startEventResponse, businessProcess.start()));
        verify(externalTaskService).complete(mockTask, variables);
    }

    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, names = {"READY", "DISPATCHED"})
    void shouldStartBusinessProcess_whenValidBusinessProcessStatus_whenCaseLinkIsNotNull(BusinessProcessStatus status) {
        GaStateFlow stateFlow = mock(GaStateFlow.class);
        State state = mock(State.class);
        when(state.getName()).thenReturn("MAIN.DRAFT");
        when(stateFlow.getState()).thenReturn(state);
        when(stateFlow.getFlags()).thenReturn(Map.of());
        when(gaStateFlowEngine.evaluate(any(GeneralApplicationCaseData.class))).thenReturn(stateFlow);

        BusinessProcess businessProcess = new BusinessProcess().setStatus(status);
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
            .businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();
        variables.putValue("generalAppParentCaseLink", caseData.getGeneralAppParentCaseLink().getCaseReference());

        when(caseDetailsConverter.toGeneralApplicationCaseData(any(CaseDetails.class))).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(CASE_ID, START_GA_BUSINESS_PROCESS))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.submitGaUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_GA_BUSINESS_PROCESS);
        verify(coreCaseDataService).submitGaUpdate(CASE_ID, content(startEventResponse, businessProcess.start()));
        verify(externalTaskService).complete(mockTask, variables);
    }

    @Test
    void shouldNotUpdateBusinessProcess_whenInputStatusIsStartedAndHaveDifferentProcessInstanceId() {
        GaStateFlow stateFlow = mock(GaStateFlow.class);
        State state = mock(State.class);
        when(state.getName()).thenReturn("MAIN.DRAFT");
        when(stateFlow.getState()).thenReturn(state);
        when(stateFlow.getFlags()).thenReturn(Map.of());
        when(gaStateFlowEngine.evaluate(any(GeneralApplicationCaseData.class))).thenReturn(stateFlow);

        BusinessProcess businessProcess = getBusinessProcess(STARTED, "differentId");
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft().businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(any(CaseDetails.class))).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(CASE_ID, START_GA_BUSINESS_PROCESS))
            .thenReturn(startEventResponse);

        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_GA_BUSINESS_PROCESS);
        verify(externalTaskService).complete(mockTask, variables);
        verify(coreCaseDataService, never()).submitUpdate(anyString(), any(CaseDataContent.class));
    }

    @Test
    void shouldRaiseBpmnError_whenBusinessProcessStatusIsStarted_AndHaveSameProcessInstanceId() {
        BusinessProcess businessProcess = getBusinessProcess(STARTED, PROCESS_INSTANCE_ID);
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft().businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(any(CaseDetails.class))).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(CASE_ID, START_GA_BUSINESS_PROCESS))
            .thenReturn(startEventResponse);

        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_GA_BUSINESS_PROCESS);
        verify(coreCaseDataService, never()).submitGaUpdate(eq(CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
        verify(externalTaskService).handleBpmnError(mockTask, ERROR_CODE);
    }

    @Test
    void shouldRaiseBpmnError_whenBusinessProcessStatusIsFinished() {
        BusinessProcess businessProcess = getBusinessProcess(FINISHED, PROCESS_INSTANCE_ID);
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft().businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(any(CaseDetails.class))).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(CASE_ID, START_GA_BUSINESS_PROCESS))
            .thenReturn(startEventResponse);

        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_GA_BUSINESS_PROCESS);
        verify(coreCaseDataService, never()).submitGaUpdate(anyString(), any(CaseDataContent.class));
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
