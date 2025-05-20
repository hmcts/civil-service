package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DISMISS_CASE_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class CaseDismissClaimantDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CaseDismissClaimantDashboardNotificationHandler handler;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;

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
        HashMap<String, Object> scenarioParams = new HashMap<>();
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isLipQueryManagementEnabled(any())).thenReturn(false);
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

        // When
        handler.handle(params);
        final String caseId = caseData.getCcdCaseReference().toString();
        // Then
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            caseId,
            "CLAIMANT"
        );
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseId,
            "CLAIMANT"
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_DISMISS_CASE_CLAIMANT.getScenario(),
            caseId,
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldRecordQMNotificationScenario_whenInvokedWhileClaimantCitizenHasAnOpenQuery() {
        // Given
        LocalDateTime queryDate = LocalDateTime.now();
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .includesApplicantCitizenQueryFollowUp(queryDate)
            .build().toBuilder()
            .ccdCaseReference(1234L)
            .previousCCDState(AWAITING_APPLICANT_INTENTION).build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DISMISS_CASE_CLAIMANT.name()).build()
        ).build();
        HashMap<String, Object> scenarioParams = new HashMap<>();
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isLipQueryManagementEnabled(any())).thenReturn(true);
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

        // When
        handler.handle(params);
        final String caseId = caseData.getCcdCaseReference().toString();
        // Then
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_DISMISS_CASE_CLAIMANT.getScenario(),
            caseId,
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_CLAIMANT.getScenario(),
            caseId,
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }
}
