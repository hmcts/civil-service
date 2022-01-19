package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
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
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.START_BUSINESS_PROCESS_GASPEC;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.FINISHED;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.STARTED;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;
import static uk.gov.hmcts.reform.civil.handler.tasks.StartGeneralApplicationBusinessProcessTaskHandler.FLOW_STATE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    StartGeneralApplicationBusinessProcessTaskHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
@ExtendWith(SpringExtension.class)
public class StartGeneralApplicationBusinessProcessTaskHandlerTest extends BaseCallbackHandlerTest {
    private static final String CASE_ID = "1";
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";
    public static final String ERROR_CODE = "ABORT";

    @Mock
    private ExternalTask mockTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @Autowired
    private StartGeneralApplicationBusinessProcessTaskHandler handler;

    private final VariableMap variables = Variables.createVariables();

    private CaseData getFinishedTestCaseDataWithProcessID(CaseData caseData) {
        GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
        GeneralApplication application = builder
            .businessProcess(BusinessProcess.builder()
                                 .status(FINISHED)
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .build()).build();
        return caseData.toBuilder()
            .generalApplications(wrapElements(application))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .build())
            .build();
    }

    private CaseData getTestCaseData(CaseData caseData) {
        GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
        GeneralApplication application = builder
            .businessProcess(BusinessProcess.builder()
                                 .status(BusinessProcessStatus.READY)
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .build()).build();
        return caseData.toBuilder()
            .generalApplications(wrapElements(application))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .build())
            .build();
    }

    private CaseData getTestCaseDataWithMultipleGA(CaseData caseData) {
        GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
        GeneralApplication application = builder
            .businessProcess(BusinessProcess.builder()
                                 .status(BusinessProcessStatus.READY)
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .build()).build();

        GeneralApplication application2 = builder
            .businessProcess(BusinessProcess.builder()
                                 .status(BusinessProcessStatus.READY)
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .build()).build();

        return caseData.toBuilder()
            .generalApplications(wrapElements(application))
            .generalApplications(wrapElements(application2))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .build())
            .build();
    }

    private CaseData getStartedTestCaseData(CaseData caseData) {
        GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
        GeneralApplication application = builder
            .businessProcess(BusinessProcess.builder()
                                 .status(STARTED).processInstanceId("differentID")
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .build()).build();
        return caseData.toBuilder()
            .generalApplications(wrapElements(application))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .build())
            .build();
    }

    @BeforeEach
    void init() {
        variables.putValue(FLOW_STATE, "MAIN.DRAFT");
        variables.putValue(FLOW_FLAGS, Map.of());
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
        when(mockTask.getActivityId()).thenReturn("activityId");
        when(mockTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);

        when(mockTask.getAllVariables()).thenReturn(Map.of(
            "caseId", CASE_ID,
            "caseEvent", START_BUSINESS_PROCESS_GASPEC.name()
        ));
    }

    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, names = {"READY", "DISPATCHED"})
    void shouldStartBusinessProcess_whenValidBusinessProcessStatusWithOneGA(BusinessProcessStatus status) {

        CaseData caseData = getTestCaseData(CaseDataBuilder.builder().build());
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startUpdate(CASE_ID, START_BUSINESS_PROCESS_GASPEC)).thenReturn(startEventResponse);
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);
        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_BUSINESS_PROCESS_GASPEC);
        verify(coreCaseDataService).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockTask, variables);

    }

    @Test
    void shouldStartBusinessProcess_whenValidBusinessProcessStatusWithMultipleGA() {

        CaseData caseData = getTestCaseDataWithMultipleGA(CaseDataBuilder.builder().build());
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startUpdate(CASE_ID, START_BUSINESS_PROCESS_GASPEC)).thenReturn(startEventResponse);
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);
        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_BUSINESS_PROCESS_GASPEC);
        verify(coreCaseDataService).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockTask, variables);

    }

    @Test
    void shouldNotUpdateBusinessProcess_whenInputStatusIsStartedAndHaveDifferentProcessInstanceId() {
        CaseData caseData = getStartedTestCaseData(CaseDataBuilder.builder().build());
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startUpdate(CASE_ID, START_BUSINESS_PROCESS_GASPEC)).thenReturn(startEventResponse);
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_BUSINESS_PROCESS_GASPEC);
        verify(externalTaskService).complete(mockTask, variables);
        verify(coreCaseDataService, never()).submitUpdate(anyString(), any(CaseDataContent.class));
    }

    @Test
    void shouldRaiseBpmnError_whenBusinessProcessStatusIsFinished() {
        CaseData caseData = getFinishedTestCaseDataWithProcessID(CaseDataBuilder.builder().build());
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startUpdate(CASE_ID, START_BUSINESS_PROCESS_GASPEC)).thenReturn(startEventResponse);

        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, START_BUSINESS_PROCESS_GASPEC);
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
}
