package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.stateflow.GaStateFlow;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_JUDGES_FORM;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_STATE;

@ExtendWith(MockitoExtension.class)
class ApplicationProcessCaseEventTaskHandlerTest {

    private static final String CASE_ID = "1";
    private static final String PARENT_CASE_ID = "2";

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;

    @Mock
    private CamundaRuntimeClient camundaRuntimeClient;

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);

    @Mock
    private GaStateFlowEngine gaStateFlowEngine;

    private ApplicationProcessCaseEventTaskHandler applicationProcessCaseEventTaskHandler;

    @BeforeEach
    void setUp() {
        EventProperties eventProperties = new EventProperties();
        eventProperties.setRetryCount(3);
        applicationProcessCaseEventTaskHandler = new ApplicationProcessCaseEventTaskHandler(
            new ExternalTaskCompletionService(),
            eventProperties,
            caseDetailsConverter,
            gaStateFlowEngine,
            coreCaseDataService,
            objectMapper,
            camundaRuntimeClient
        );

        when(mockTask.getTopicName()).thenReturn("test");
    }

    @Nested
    class NotifyRespondent {

        @BeforeEach
        void init() {
            Map<String, Object> variables = Map.of(
                "caseId", CASE_ID,
                "caseEvent", GENERATE_JUDGES_FORM.name()
            );

            when(mockTask.getAllVariables()).thenReturn(variables);
        }

        @Test
        void shouldTriggerCCDEvent_whenHandlerIsExecuted() {
            when(mockTask.getActivityId()).thenReturn("CreatePDFDocument");
            GaStateFlow stateFlow = mock(GaStateFlow.class);
            State state = mock(State.class);
            when(state.getName()).thenReturn("MAIN.DRAFT");
            when(stateFlow.getState()).thenReturn(state);
            when(stateFlow.getFlags()).thenReturn(Map.of());
            when(gaStateFlowEngine.evaluate(any(GeneralApplicationCaseData.class))).thenReturn(stateFlow);

            GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
                .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
                .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_ID))
                .build();
            VariableMap variables = Variables.createVariables();
            variables.putValue(FLOW_STATE, "MAIN.DRAFT");
            variables.putValue(FLOW_FLAGS, Map.of());
            variables.putValue("generalAppParentCaseLink", PARENT_CASE_ID);

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(coreCaseDataService.startGaUpdate(CASE_ID, GENERATE_JUDGES_FORM))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());

            when(coreCaseDataService.submitGaUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

            applicationProcessCaseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService).startGaUpdate(CASE_ID, GENERATE_JUDGES_FORM);
            verify(coreCaseDataService).submitGaUpdate(eq(CASE_ID), any(CaseDataContent.class));
            verify(externalTaskService).complete(mockTask, variables);
        }

        @ParameterizedTest
        @CsvSource({
            "process-1, CreatePDFDocument, false, false",
            "process-2, CreatePDFDocument, false, true",
            "process-2, CreatePDFDocument, true, false",
            "process-1, PreviousActivity, false, true",
            "process-2, PreviousActivity, false, true"
        })
        void shouldOnlySubmitEvent_whenTaskHasNotAlreadyCommitted(String processInstanceId,
                                                                String activityId,
                                                                boolean completedChild,
                                                                boolean shouldSubmit) {
            when(mockTask.getProcessInstanceId()).thenReturn("process-1");
            when(mockTask.getActivityId()).thenReturn("CreatePDFDocument");
            if (!"process-1".equals(processInstanceId) && "CreatePDFDocument".equals(activityId)) {
                when(camundaRuntimeClient.isCompletedChildProcess(
                    processInstanceId, "process-1", "GA_StartGeneralApplicationBusinessProcess"))
                    .thenReturn(completedChild);
            }
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
                .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.STARTED)
                                     .setProcessInstanceId(processInstanceId).setActivityId(activityId))
                .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_ID))
                .build();
            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
            when(coreCaseDataService.startGaUpdate(CASE_ID, GENERATE_JUDGES_FORM))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());
            if (shouldSubmit) {
                when(coreCaseDataService.submitGaUpdate(eq(CASE_ID), any(CaseDataContent.class)))
                    .thenReturn(caseData);
            }
            GaStateFlow stateFlow = mock(GaStateFlow.class);
            State state = mock(State.class);
            when(state.getName()).thenReturn("MAIN.DRAFT");
            when(stateFlow.getState()).thenReturn(state);
            when(stateFlow.getFlags()).thenReturn(Map.of());
            when(gaStateFlowEngine.evaluate(any(GeneralApplicationCaseData.class))).thenReturn(stateFlow);

            applicationProcessCaseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService, times(shouldSubmit ? 1 : 0))
                .submitGaUpdate(eq(CASE_ID), any(CaseDataContent.class));
            VariableMap expectedVariables = Variables.createVariables();
            expectedVariables.putValue(FLOW_STATE, "MAIN.DRAFT");
            expectedVariables.putValue(FLOW_FLAGS, Map.of());
            expectedVariables.putValue("generalAppParentCaseLink", PARENT_CASE_ID);
            verify(externalTaskService).complete(mockTask, expectedVariables);
            if (shouldSubmit) {
                ArgumentCaptor<CaseDataContent> content = ArgumentCaptor.forClass(CaseDataContent.class);
                verify(coreCaseDataService).submitGaUpdate(eq(CASE_ID), content.capture());
                BusinessProcess submittedProcess = (BusinessProcess) ((Map<?, ?>) content.getValue().getData()).get("businessProcess");
                assertEquals("process-1", submittedProcess.getProcessInstanceId());
                assertEquals("CreatePDFDocument", submittedProcess.getActivityId());
            }
            if ("process-1".equals(processInstanceId) || !"CreatePDFDocument".equals(activityId)) {
                verifyNoInteractions(camundaRuntimeClient);
            }
        }

        @Test
        void shouldFailWithoutSubmittingOrCompleting_whenHistoryLookupFails() {
            when(mockTask.getProcessInstanceId()).thenReturn("parent");
            when(mockTask.getActivityId()).thenReturn("CreatePDFDocument");
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
                .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.STARTED)
                                     .setProcessInstanceId("child").setActivityId("CreatePDFDocument"))
                .build();
            when(coreCaseDataService.startGaUpdate(CASE_ID, GENERATE_JUDGES_FORM))
                .thenReturn(StartEventResponse.builder()
                                .caseDetails(CaseDetailsBuilder.builder().data(caseData).build()).build());
            when(camundaRuntimeClient.isCompletedChildProcess(
                "child", "parent", "GA_StartGeneralApplicationBusinessProcess"))
                .thenThrow(new IllegalStateException("History unavailable"));
            when(mockTask.getRetries()).thenReturn(1);

            applicationProcessCaseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService, never()).submitGaUpdate(any(), any());
            verify(externalTaskService, never()).complete(eq(mockTask), anyMap());
            verify(externalTaskService).handleFailure(
                eq(mockTask), eq("History unavailable"), anyString(), eq(0), anyLong());
        }

        @Test
        void shouldCallHandleFailureMethod_whenExceptionFromBusinessLogic() {
            String errorMessage = "there was an error";

            when(mockTask.getRetries()).thenReturn(null);
            when(coreCaseDataService.startGaUpdate(CASE_ID, GENERATE_JUDGES_FORM))
                .thenAnswer(invocation -> {
                    throw new Exception(errorMessage);
                });

            applicationProcessCaseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(externalTaskService, never()).complete(mockTask);
            verify(externalTaskService).handleFailure(
                eq(mockTask),
                eq(errorMessage),
                anyString(),
                eq(2),
                anyLong()
            );
        }

        @Test
        void shouldCallHandleFailureMethod_whenFeignExceptionFromUnprocessableContent() {
            String errorMessage = "there was an error";
            int status = 422;
            Request.HttpMethod requestType = Request.HttpMethod.POST;
            String exampleUrl = "example url";

            when(coreCaseDataService.startGaUpdate(CASE_ID, GENERATE_JUDGES_FORM))
                .thenAnswer(invocation -> {
                    throw FeignException.errorStatus(errorMessage, Response.builder()
                        .request(
                            Request.create(
                                requestType,
                                exampleUrl,
                                new HashMap<>(), //this field is required for construtor//
                                null,
                                null,
                                null
                            ))
                        .status(status)
                        .build());
                });

            applicationProcessCaseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(externalTaskService, never()).complete(mockTask);
            verify(externalTaskService).handleFailure(
                eq(mockTask),
                eq(String.format("[%s] during [%s] to [%s] [%s]: []", status, requestType, exampleUrl, errorMessage)),
                anyString(),
                eq(0),
                anyLong()
            );
        }

        @Test
        void shouldCallHandleFailureMethod_whenExceptionOnCompleteCall() {
            when(mockTask.getActivityId()).thenReturn("CreatePDFDocument");
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
                .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
                .build();
            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
            when(coreCaseDataService.startGaUpdate(any(), any()))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());
            when(coreCaseDataService.submitGaUpdate(any(), any()))
                .thenReturn(new GeneralApplicationCaseData().generalAppParentCaseLink(
                    new GeneralAppParentCaseLink().setCaseReference("123")).build());

            applicationProcessCaseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(externalTaskService, times(1)).handleFailure(
                any(ExternalTask.class),
                anyString(),
                anyString(),
                anyInt(),
                anyLong()
            );
        }
    }
}
