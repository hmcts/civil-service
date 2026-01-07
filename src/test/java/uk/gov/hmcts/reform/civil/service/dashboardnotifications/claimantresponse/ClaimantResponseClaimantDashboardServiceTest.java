package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER_TOKEN";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;

    private ClaimantResponseClaimantDashboardService service;

    @BeforeEach
    void setup() {
        service = new ClaimantResponseClaimantDashboardService(
            dashboardScenariosService,
            mapper,
            featureToggleService,
            dashboardNotificationService,
            taskListService
        );
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordCaseSettledScenarioAndClearClaimantTasks() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234L)
            .ccdState(CASE_SETTLED)
            .applicant1Represented(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "CLAIMANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("1234", "CLAIMANT");
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordGeneralApplicationScenarioWhenProceedingInHeritageSystem() {
        ClaimantLiPResponse response = ClaimantLiPResponse.builder()
            .claimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE)
            .build();
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1LiPResponse(response)
            .build();

        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234L)
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .applicant1Represented(YesOrNo.NO)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verifyNoInteractions(dashboardNotificationService, taskListService);
    }

    @Test
    void shouldSkipScenarioWhenApplicantRepresented() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234L)
            .ccdState(CASE_SETTLED)
            .applicant1Represented(YesOrNo.YES)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }
}
