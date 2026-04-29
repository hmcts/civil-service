package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimsettled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLE_EVENT_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class ClaimSettledDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";

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

    @InjectMocks
    private ClaimSettledDefendantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioAndInactiveGAItemsWhenLocationNotWhiteListed() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .ccdCaseReference(1234L)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("00001"))
            .build();

        when(featureToggleService.isLocationWhiteListed("00001")).thenReturn(false);

        service.notifyClaimSettled(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "DEFENDANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(
            "1234", "DEFENDANT", "Application.View"
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLE_EVENT_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioButNotInactiveGAItemsWhenLocationWhiteListed() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .ccdCaseReference(1234L)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("00001"))
            .build();

        when(featureToggleService.isLocationWhiteListed("00001")).thenReturn(true);

        service.notifyClaimSettled(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService, never()).deleteByReferenceAndCitizenRole(any(), any());
        verify(taskListService, never()).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(
            any(), any(), any()
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLE_EVENT_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldSkipRepresentedDefendant() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.YES)
            .ccdCaseReference(1234L)
            .build();

        service.notifyClaimSettled(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
        verify(dashboardNotificationService, never()).deleteByReferenceAndCitizenRole(any(), any());
        verify(taskListService, never()).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(
            any(), any(), any()
        );
    }
}
