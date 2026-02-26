package uk.gov.hmcts.reform.civil.service.dashboardnotifications.staycase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_CASE_STAYED_CLAIMANT;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StayCaseClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private StayCaseClaimantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldNotifyClaimantWhenCaseStayed() {
        CaseData caseData = CaseDataBuilder.builder().build()
            .setCcdCaseReference(1234L)
            .setPreStayState("IN_MEDIATION");

        service.notifyStayCase(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "CLAIMANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            "1234", "CLAIMANT", "Applications");
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_CASE_STAYED_CLAIMANT.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotDeleteNotification_WhenPreStayStateIsAwaitingRespondentAcknowledgement() {
        CaseData caseData = CaseDataBuilder.builder().build()
            .setCcdCaseReference(5678L)
            .setPreStayState("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");

        service.notifyStayCase(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService, never()).deleteByReferenceAndCitizenRole(any(), any());
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            "5678", "CLAIMANT", "Applications"
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CP_CASE_STAYED_CLAIMANT.getScenario()),
            eq("5678"),
            eq(ScenarioRequestParams.builder().params(new HashMap<>()).build())
        );
    }

    @Test
    void shouldNotDeleteNotification_WhenPreStayStateIsAwaitingApplicationIntention() {
        CaseData caseData = CaseDataBuilder.builder().build()
            .setCcdCaseReference(5678L)
            .setPreStayState("AWAITING_APPLICANT_INTENTION");

        service.notifyStayCase(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService, never()).deleteByReferenceAndCitizenRole(any(), any());
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            "5678", "CLAIMANT", "Applications"
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CP_CASE_STAYED_CLAIMANT.getScenario()),
            eq("5678"),
            eq(ScenarioRequestParams.builder().params(new HashMap<>()).build())
        );
    }
}
