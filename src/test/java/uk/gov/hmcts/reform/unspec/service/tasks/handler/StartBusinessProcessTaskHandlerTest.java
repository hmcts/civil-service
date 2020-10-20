package uk.gov.hmcts.reform.unspec.service.tasks.handler;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.START_BUSINESS_PROCESS;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.FINISHED;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.STARTED;
import static uk.gov.hmcts.reform.unspec.service.tasks.handler.StartBusinessProcessTaskHandler.BUSINESS_PROCESS;
import static uk.gov.hmcts.reform.unspec.service.tasks.handler.StartBusinessProcessTaskHandler.FLOW_STATE;

@SpringBootTest(classes = {
    StartBusinessProcessTaskHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
@ExtendWith(SpringExtension.class)
class StartBusinessProcessTaskHandlerTest {

    private static final String CASE_ID = "1";
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    @Mock
    private ExternalTask mockExternalTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @Autowired
    private StartBusinessProcessTaskHandler startBusinessProcessTaskHandler;

    private VariableMap variables = Variables.createVariables();

    @BeforeEach
    void init() {
        variables.putValue(FLOW_STATE, "MAIN.DRAFT");
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getWorkerId()).thenReturn("worker");
        when(mockExternalTask.getActivityId()).thenReturn("activityId");
        when(mockExternalTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);

        when(mockExternalTask.getAllVariables()).thenReturn(Map.of(
            "caseId", CASE_ID,
            "caseEvent", START_BUSINESS_PROCESS.name()
        ));
    }

    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, names = {"READY", "DISPATCHED"})
    void shouldStartBusinessProcess_whenValidBusinessProcessStatus(BusinessProcessStatus status) {
        BusinessProcess businessProcess = BusinessProcess.builder().status(status).build();
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startUpdate(eq(CASE_ID), eq(START_BUSINESS_PROCESS))).thenReturn(startEventResponse);
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

        startBusinessProcessTaskHandler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(eq(CASE_ID), eq(START_BUSINESS_PROCESS));

        verify(coreCaseDataService)
            .submitUpdate(eq(CASE_ID), eq(caseDataContent(startEventResponse, businessProcess.start())));

        verify(externalTaskService).complete(eq(mockExternalTask), eq(variables));
    }

    @Test
    void shouldNotUpdateBusinessProcess_whenInputStatusIsStartedAndHaveDifferentProcessInstanceId() {
        BusinessProcess businessProcess = BusinessProcess.builder().status(STARTED).processInstanceId("differentId")
            .build();

        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();
        when(coreCaseDataService.startUpdate(eq(CASE_ID), eq(START_BUSINESS_PROCESS))).thenReturn(startEventResponse);

        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

        startBusinessProcessTaskHandler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(eq(CASE_ID), eq(START_BUSINESS_PROCESS));
        verify(externalTaskService).complete(eq(mockExternalTask), eq(variables));
        verify(coreCaseDataService, never()).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
    }

    @Test
    void shouldRaiseBpmnError_whenBusinessProcessStatusIsStarted_AndHaveSameProcessInstanceId() {
        BusinessProcess businessProcess = BusinessProcess.builder()
            .status(STARTED)
            .processInstanceId(PROCESS_INSTANCE_ID)
            .build();

        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startUpdate(eq(CASE_ID), eq(START_BUSINESS_PROCESS))).thenReturn(startEventResponse);

        assertThrows(
            BpmnError.class,
            () -> startBusinessProcessTaskHandler.execute(mockExternalTask, externalTaskService),
            "ABORT"
        );

        verify(coreCaseDataService).startUpdate(eq(CASE_ID), eq(START_BUSINESS_PROCESS));
        verify(coreCaseDataService, never()).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
    }

    @Test
    void shouldRaiseBpmnError_whenBusinessProcessStatusIsFinished() {
        BusinessProcess businessProcess = BusinessProcess.builder()
            .status(FINISHED)
            .processInstanceId(PROCESS_INSTANCE_ID)
            .build();

        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().businessProcess(businessProcess).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startUpdate(eq(CASE_ID), eq(START_BUSINESS_PROCESS))).thenReturn(startEventResponse);

        assertThrows(
            BpmnError.class,
            () -> startBusinessProcessTaskHandler.execute(mockExternalTask, externalTaskService),
            "ABORT"
        );

        verify(coreCaseDataService).startUpdate(eq(CASE_ID), eq(START_BUSINESS_PROCESS));
        verify(coreCaseDataService, never()).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, BusinessProcess businessProcess) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.put(BUSINESS_PROCESS, businessProcess);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(data)
            .build();
    }
}
