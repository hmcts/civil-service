package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DECISION_REQUEST_FOR_RECONSIDERATION_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class DecisionOnRequestForReconsiderationDefendantHandlerTest {

    @InjectMocks
    private DecisionOnRequestForReconsiderationDefendantHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    public static final String TASK_ID = "GenerateDashboardNotificationDecisionReconsiderationDefendant";

    HashMap<String, Object> params = new HashMap<>();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(
            CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_DEFENDANT1);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_DEFENDANT1.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    public void shouldCreateDashboardNotifications_whenDashboardIsEnabled() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateTrialReadyCheckLiP(false)
            .respondent1Represented(YesOrNo.NO)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_DECISION_REQUEST_FOR_RECONSIDERATION_DEFENDANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    public void shouldNotCreateDashboardNotifications_whenDefendantRepresented() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateTrialReadyCheckLiP(false)
            .respondent1Represented(YesOrNo.YES)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verifyNoInteractions(dashboardApiClient);
    }
}
