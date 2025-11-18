package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseProceedOfflineDashboardServiceTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;

    private TestDashboardService dashboardService;

    @BeforeEach
    void setup() {
        dashboardService = new TestDashboardService(dashboardScenariosService, mapper, dashboardNotificationService, taskListService);
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioWhenCaseManStateEligible() {
        dashboardService.casemanEligible = true;
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(123L)
            .previousCCDState(CaseState.AWAITING_APPLICANT_INTENTION)
            .build();

        dashboardService.notifyCaseProceedOffline(caseData, "auth");

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("123", "ROLE");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory("123", "ROLE", "Applications");
        verify(dashboardScenariosService).recordScenarios(
            eq("auth"),
            eq("Primary"),
            eq("123"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioWhenProgressionStateEligible() {
        dashboardService.progressionEligible = true;
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(456L)
            .previousCCDState(CaseState.CASE_PROGRESSION)
            .build();

        dashboardService.notifyCaseProceedOffline(caseData, "auth");

        verify(dashboardScenariosService).recordScenarios(
            eq("auth"),
            eq("Primary"),
            eq("456"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldSkipWhenNotEligible() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(999L)
            .build();

        dashboardService.notifyCaseProceedOffline(caseData, "auth");

        verifyNoInteractions(dashboardNotificationService);
        verifyNoInteractions(dashboardScenariosService);
    }

    private static class TestDashboardService extends CaseProceedOfflineDashboardService {

        boolean casemanEligible;
        boolean progressionEligible;

        TestDashboardService(DashboardScenariosService dashboardScenariosService,
                             DashboardNotificationsParamsMapper mapper,
                             DashboardNotificationService dashboardNotificationService,
                             TaskListService taskListService) {
            super(dashboardScenariosService, mapper, dashboardNotificationService, taskListService);
        }

        @Override
        protected String getScenario(CaseData caseData) {
            return "Primary";
        }

        @Override
        protected java.util.Map<String, Boolean> getScenarios(CaseData caseData) {
            return java.util.Map.of();
        }

        @Override
        protected String citizenRole() {
            return "ROLE";
        }

        @Override
        protected boolean eligibleForCasemanState(CaseData caseData) {
            return casemanEligible;
        }

        @Override
        protected boolean eligibleForCaseProgressionState(CaseData caseData) {
            return progressionEligible;
        }
    }
}
