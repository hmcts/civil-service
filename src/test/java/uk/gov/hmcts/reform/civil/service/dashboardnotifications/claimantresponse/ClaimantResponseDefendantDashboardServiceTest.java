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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_CLAIMANT_REJECTS_DEF_PLAN_CLAIMANT_DISAGREES_COURT_PLAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseDefendantDashboardServiceTest {

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

    private ClaimantResponseDefendantDashboardService service;

    @BeforeEach
    void setup() {
        service = new ClaimantResponseDefendantDashboardService(
            dashboardScenariosService,
            mapper,
            featureToggleService,
            dashboardNotificationService,
            taskListService
        );
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordCaseSettledScenarioAndClearDefendantTasks() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234L)
            .ccdState(CASE_SETTLED)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .respondent1Represented(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "DEFENDANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("1234", "DEFENDANT");
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario()),
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
            .respondent1Represented(YesOrNo.NO)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_CLAIMANT_REJECTS_DEF_PLAN_CLAIMANT_DISAGREES_COURT_PLAN_DEFENDANT
                .getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verifyNoInteractions(dashboardNotificationService, taskListService);
    }

    @Test
    void shouldSkipScenarioWhenRespondentRepresented() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234L)
            .ccdState(CASE_SETTLED)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }
}
