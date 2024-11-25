package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.CaseDismissClaimantDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;

@ExtendWith(MockitoExtension.class)
class CaseDismissClaimantDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CaseDismissClaimantDashboardNotificationHandler handler;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @Test
    void shouldRecordScenario_whenInvoked() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .ccdCaseReference(1234L)
            .previousCCDState(AWAITING_APPLICANT_INTENTION).build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DISMISS_CASE_CLAIMANT.name()).build()
        ).build();
        when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);
        HashMap<String, Object> scenarioParams = new HashMap<>();
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

        // When
        handler.handle(params);

        // Then
        verify(dashboardApiClient).deleteNotificationsForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "CLAIMANT",
            "BEARER_TOKEN"
        );
        verify(dashboardApiClient).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "CLAIMANT",
            "BEARER_TOKEN"
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            DashboardScenarios.SCENARIO_AAA6_DISMISS_CASE_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }
}
