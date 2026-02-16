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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.ga.stateflow.GaStateFlow;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperBuilder;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;

@ExtendWith(MockitoExtension.class)
class GaCaseEventTaskHandlerTest {

    private static final String CASE_ID = "1";

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;

    @Mock
    private GaStateFlowEngine gaStateFlowEngine;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private GaCaseEventTaskHandler caseEventTaskHandler;

    @Spy
    private ObjectMapper objectMapper = ObjectMapperBuilder.instance();

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
    }

    @Nested
    class NotifyRespondent {

        @BeforeEach
        void init() {
            Map<String, Object> variables = Map.of(
                "caseId", CASE_ID,
                "caseEvent", INITIATE_GENERAL_APPLICATION.name()
            );

            when(mockTask.getAllVariables()).thenReturn(variables);
        }

        @Test
        void shouldTriggerCCDEvent_whenHandlerIsExecuted() {
            GaStateFlow stateFlow = mock(GaStateFlow.class);
            State state = mock(State.class);
            when(state.getName()).thenReturn("MAIN.DRAFT");
            when(stateFlow.getState()).thenReturn(state);
            when(stateFlow.getFlags()).thenReturn(Map.of());
            when(gaStateFlowEngine.evaluate(any(GeneralApplicationCaseData.class))).thenReturn(stateFlow);

            CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
                .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
                .build();
            GeneralApplicationCaseData gaCaseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
                .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
                .build();
            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);
            when(coreCaseDataService.startUpdate(CASE_ID, INITIATE_GENERAL_APPLICATION))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());
            when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(gaCaseData);

            caseEventTaskHandler.execute(mockTask, externalTaskService);

            VariableMap variables = Variables.createVariables();
            variables.putValue(BaseExternalTaskHandler.FLOW_STATE, "MAIN.DRAFT");
            variables.putValue(FLOW_FLAGS, Map.of());
            verify(coreCaseDataService).startUpdate(CASE_ID, INITIATE_GENERAL_APPLICATION);
            verify(coreCaseDataService).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
            verify(externalTaskService).complete(mockTask, variables);
        }

        @Test
        void shouldCallHandleFailureMethod_whenExceptionFromBusinessLogic() {
            String errorMessage = "there was an error";

            when(mockTask.getRetries()).thenReturn(null);
            when(coreCaseDataService.startUpdate(CASE_ID, INITIATE_GENERAL_APPLICATION))
                .thenAnswer(invocation -> {
                    throw new Exception(errorMessage);
                });

            caseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(externalTaskService, never()).complete(mockTask);
            verify(externalTaskService).handleFailure(
                eq(mockTask),
                eq(errorMessage),
                anyString(),
                eq(2),
                eq(300000L)
            );
        }

        @Test
        void shouldCallHandleFailureMethod_whenFeignExceptionFromBusinessLogic() {
            String errorMessage = "there was an error";
            int status = 422;
            Request.HttpMethod requestType = Request.HttpMethod.POST;
            String exampleUrl = "example url";

            when(mockTask.getRetries()).thenReturn(null);
            when(coreCaseDataService.startUpdate(CASE_ID, INITIATE_GENERAL_APPLICATION))
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

            caseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(externalTaskService, never()).complete(mockTask);
            verify(externalTaskService).handleFailure(
                eq(mockTask),
                eq(String.format("[%s] during [%s] to [%s] [%s]: []", status, requestType, exampleUrl, errorMessage)),
                anyString(),
                eq(2),
                eq(300000L)
            );
        }
    }
}
