package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.RestException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;

@SpringBootTest(classes = {
    GaSpecExternalCaseEventTaskHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    GaStateFlowEngine.class
})
@ExtendWith(SpringExtension.class)
class GASpecCaseEventExternalTaskHandlerTest {

    private static final String CASE_ID = "1";

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @MockBean
    private GaCoreCaseDataService coreCaseDataService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private GaSpecExternalCaseEventTaskHandler gaSpecCaseEventTaskHandler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
        when(mockTask.getActivityId()).thenReturn("activityId");
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
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
                .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
                .build();
            VariableMap variables = Variables.createVariables();
            variables.putValue(BaseExternalTaskHandler.FLOW_STATE, "MAIN.DRAFT");
            variables.putValue(FLOW_FLAGS, Map.of());

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(coreCaseDataService.startGaUpdate(CASE_ID, INITIATE_GENERAL_APPLICATION))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());

            when(coreCaseDataService.submitGaUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

            gaSpecCaseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService).startGaUpdate(CASE_ID, INITIATE_GENERAL_APPLICATION);
            verify(coreCaseDataService).submitGaUpdate(eq(CASE_ID), any(CaseDataContent.class));
            verify(externalTaskService).complete(mockTask, variables);
        }

        @Test
        void shouldCallHandleFailureMethod_whenExceptionFromBusinessLogic() {
            String errorMessage = "there was an error";

            when(mockTask.getRetries()).thenReturn(null);
            when(coreCaseDataService.startGaUpdate(CASE_ID, INITIATE_GENERAL_APPLICATION))
                .thenAnswer(invocation -> {
                    throw new Exception(errorMessage);
                });

            gaSpecCaseEventTaskHandler.execute(mockTask, externalTaskService);

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
            when(coreCaseDataService.startGaUpdate(CASE_ID, INITIATE_GENERAL_APPLICATION))
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

            gaSpecCaseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(externalTaskService, never()).complete(mockTask);
            verify(externalTaskService).handleFailure(
                eq(mockTask),
                eq(String.format("[%s] during [%s] to [%s] [%s]: []", status, requestType, exampleUrl, errorMessage)),
                anyString(),
                eq(2),
                eq(300000L)
            );
        }

        @Test
        void shouldNotCallHandleFailureMethod_whenExceptionOnCompleteCall() {
            String errorMessage = "there was an error";
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
                .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
                .build();
            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
            when(coreCaseDataService.startGaUpdate(any(), any()))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());
            when(coreCaseDataService.submitGaUpdate(any(), any()))
                .thenReturn(GeneralApplicationCaseData.builder().generalAppParentCaseLink(
                    new GeneralAppParentCaseLink().setCaseReference("123")).build());

            doThrow(new NotFoundException(errorMessage, new RestException(null, null, null)))
                .when(externalTaskService).complete(mockTask);

            gaSpecCaseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(externalTaskService, never()).handleFailure(
                any(ExternalTask.class),
                anyString(),
                anyString(),
                anyInt(),
                anyLong()
            );
        }
    }
}
