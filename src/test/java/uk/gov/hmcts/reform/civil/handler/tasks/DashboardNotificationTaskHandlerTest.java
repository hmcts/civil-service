package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.ga.stateflow.GaStateFlow;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationTransactionalService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DASHBOARD_NOTIFICATION_EVENT;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_STATE;
import static uk.gov.hmcts.reform.civil.handler.tasks.DashboardNotificationTaskHandler.CIVIL_TOPIC;
import static uk.gov.hmcts.reform.civil.handler.tasks.DashboardNotificationTaskHandler.GA_TOPIC;
import static uk.gov.hmcts.reform.civil.utils.MarkPaidInFullUtil.checkMarkPaidInFull;

@ExtendWith(MockitoExtension.class)
class DashboardNotificationTaskHandlerTest {

    private static final String CASE_ID = "1";
    private static final String ACTIVITY_ID = "GenerateDashboardNotificationsDefendantResponse";
    private static final String PROCESS_INSTANCE_ID = "process-instance-id";
    private static final String SYSTEM_USER_TOKEN = "system-user-token";

    @Mock
    private ExternalTask mockTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private GaCoreCaseDataService gaCoreCaseDataService;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private GaStateFlowEngine gaStateFlowEngine;
    @Mock
    private DashboardNotificationTransactionalService dashboardNotificationTransactionalService;
    @Mock
    private SystemUpdateUserConfiguration userConfig;
    @Mock
    private UserService userService;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
    @Spy
    private EventProperties eventProperties = configuredEventProperties();
    @Spy
    private ExternalTaskCompletionService externalTaskCompletionService = new ExternalTaskCompletionService();
    @Captor
    private ArgumentCaptor<DashboardTaskContext> contextCaptor;

    @InjectMocks
    private DashboardNotificationTaskHandler handler;

    @Test
    void shouldDispatchCivilDashboardNotification_withoutSubmittingCcdEvent() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(new BusinessProcess()
                                 .setStatus(BusinessProcessStatus.READY)
                                 .setProcessInstanceId("old-process-instance-id"))
            .build();
        final CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        VariableMap expectedVariables = Variables.createVariables();
        expectedVariables.putValue(FLOW_STATE, "MAIN.DRAFT");
        expectedVariables.putValue(FLOW_FLAGS, Map.of());
        expectedVariables.putValue("isJudgmentMarkedPaidInFull", checkMarkPaidInFull(caseData));

        stubTask(CIVIL_TOPIC);
        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        stubSystemUserToken();
        when(stateFlowEngine.getStateFlow(any(CaseData.class)))
            .thenReturn(new StateFlowDTO().setState(State.from("MAIN.DRAFT")).setFlags(Map.of()));

        handler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService, never()).startUpdate(any(), any());
        verify(coreCaseDataService, never()).submitUpdate(any(), any());
        verify(dashboardNotificationTransactionalService).dispatch(eq(ACTIVITY_ID), contextCaptor.capture());
        assertCivilContext(contextCaptor.getValue());
        verify(externalTaskService).complete(mockTask, expectedVariables);
    }

    @Test
    void shouldDispatchGaDashboardNotification_withoutSubmittingCcdEvent() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("2"))
            .build();
        final CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        VariableMap expectedVariables = Variables.createVariables();
        expectedVariables.putValue(FLOW_STATE, "MAIN.DRAFT");
        expectedVariables.putValue(FLOW_FLAGS, Map.of());
        expectedVariables.putValue("generalAppParentCaseLink", "2");

        GaStateFlow stateFlow = mock(GaStateFlow.class);
        State state = mock(State.class);
        when(state.getName()).thenReturn("MAIN.DRAFT");
        when(stateFlow.getState()).thenReturn(state);
        when(stateFlow.getFlags()).thenReturn(Map.of());

        stubTask(GA_TOPIC);
        when(gaCoreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        stubSystemUserToken();
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);
        when(gaStateFlowEngine.evaluate(any(GeneralApplicationCaseData.class))).thenReturn(stateFlow);

        handler.execute(mockTask, externalTaskService);

        verify(gaCoreCaseDataService).getCase(Long.valueOf(CASE_ID));
        verify(gaCoreCaseDataService, never()).startGaUpdate(any(), any());
        verify(gaCoreCaseDataService, never()).submitGaUpdate(any(), any());
        verify(dashboardNotificationTransactionalService).dispatch(eq(ACTIVITY_ID), contextCaptor.capture());
        assertGaContext(contextCaptor.getValue());
        verify(externalTaskService).complete(mockTask, expectedVariables);
    }

    private void stubTask(String topic) {
        when(mockTask.getAllVariables()).thenReturn(Map.of(
            "caseId", CASE_ID,
            "caseEvent", DASHBOARD_NOTIFICATION_EVENT.name()
        ));
        when(mockTask.getTopicName()).thenReturn(topic);
        when(mockTask.getActivityId()).thenReturn(ACTIVITY_ID);
        when(mockTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
    }

    private void stubSystemUserToken() {
        when(userConfig.getUserName()).thenReturn("system-user");
        when(userConfig.getPassword()).thenReturn("password");
        when(userService.getAccessToken("system-user", "password")).thenReturn(SYSTEM_USER_TOKEN);
    }

    private void assertCivilContext(DashboardTaskContext context) {
        assertThat(context.caseType()).isEqualTo(DashboardCaseType.CIVIL);
        assertThat(context.authToken()).isEqualTo(SYSTEM_USER_TOKEN);
        assertThat(context.caseData().getBusinessProcess().getActivityId()).isEqualTo(ACTIVITY_ID);
        assertThat(context.callbackParams()).isNull();
    }

    private void assertGaContext(DashboardTaskContext context) {
        assertThat(context.caseType()).isEqualTo(DashboardCaseType.GENERAL_APPLICATION);
        assertThat(context.authToken()).isEqualTo(SYSTEM_USER_TOKEN);
        assertThat(context.generalApplicationCaseData().getBusinessProcess().getActivityId())
            .isEqualTo(ACTIVITY_ID);
        assertThat(context.callbackParams()).isNull();
    }

    private static EventProperties configuredEventProperties() {
        EventProperties eventProperties = new EventProperties();
        eventProperties.setRetryCount(1);
        eventProperties.setBackoffDelay(1);
        eventProperties.setLockDuration(1000L);
        eventProperties.setDispatchDelay(0);
        return eventProperties;
    }
}
