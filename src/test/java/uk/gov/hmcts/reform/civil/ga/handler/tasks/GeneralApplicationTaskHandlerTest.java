package uk.gov.hmcts.reform.civil.ga.handler.tasks;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.ga.stateflow.GaStateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_STATE;

@SpringBootTest(classes = {
    GeneralApplicationTaskHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    GaStateFlowEngine.class
})
@ExtendWith(SpringExtension.class)
public class GeneralApplicationTaskHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    private static final String GA_CASE_ID = "1";

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @MockitoBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockitoBean
    private GaCoreCaseDataService coreCaseDataService;

    @MockitoBean
    private GaStateFlowEngine stateFlowEngine;

    @Mock
    private GaStateFlow mockedStateFlow;

    @Autowired
    private GeneralApplicationTaskHandler generalApplicationTaskHandler;

    private final CaseDataContent caseDataContent = CaseDataContent.builder().build();

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
        when(mockTask.getActivityId()).thenReturn("activityId");
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(mockedStateFlow.getState()).thenReturn(State.from("PENDING"));
        when(stateFlowEngine.evaluate(any(GeneralApplicationCaseData.class))).thenReturn(mockedStateFlow);
    }

    @Nested
    class GeneralApplicationTask {

        @BeforeEach
        void init() {
            Map<String, Object> variables = Map.of(
                "generalApplicationCaseId", GA_CASE_ID,
                "caseEvent", LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE.name()
            );

            when(mockTask.getAllVariables()).thenReturn(variables);
        }

        @Test
        void shouldLinkGAParentCaseLinkWhenParentCaseReferenceIsNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();

            VariableMap variables = Variables.createVariables();
            variables.putValue(FLOW_STATE, "MAIN.DRAFT");
            variables.putValue(FLOW_FLAGS, Map.of());

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
            StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

            when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails()))
                .thenReturn(caseData);

            when(coreCaseDataService.startGaUpdate(GA_CASE_ID, LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());

            when(coreCaseDataService.submitGaUpdate(any(String.class), any(CaseDataContent.class)))
                .thenReturn(caseData);

            coreCaseDataService.submitGaUpdate(GA_CASE_ID, caseDataContent);

            generalApplicationTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService).startGaUpdate(GA_CASE_ID, LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE);

            verify(caseDetailsConverter).toGeneralApplicationCaseData(startEventResponse.getCaseDetails());

            verify(coreCaseDataService).submitGaUpdate(GA_CASE_ID, caseDataContent);
        }

        @Test
        void shouldLinkGAParentCaseLinkWhenParentCaseReferenceIsEmpty() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().build())
                .build();

            VariableMap variables = Variables.createVariables();
            variables.putValue(FLOW_STATE, "MAIN.DRAFT");
            variables.putValue(FLOW_FLAGS, Map.of());

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
            StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

            when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails()))
                .thenReturn(caseData);

            when(coreCaseDataService.startGaUpdate(GA_CASE_ID, LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());

            when(coreCaseDataService.submitGaUpdate(any(String.class), any(CaseDataContent.class)))
                .thenReturn(caseData);

            coreCaseDataService.submitGaUpdate(GA_CASE_ID, caseDataContent);

            generalApplicationTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService).startGaUpdate(GA_CASE_ID, LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE);

            verify(caseDetailsConverter).toGeneralApplicationCaseData(startEventResponse.getCaseDetails());

            verify(coreCaseDataService).submitGaUpdate(GA_CASE_ID, caseDataContent);
        }
    }
}
