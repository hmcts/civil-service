package uk.gov.hmcts.reform.civil.service.dashboardnotifications.decisionoutcome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_TRIAL_READY_DECISION_OUTCOME;

@ExtendWith(MockitoExtension.class)
class DecisionOutcomeDefendantDashboardServiceTest {

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
    private DecisionOutcomeDefendantDashboardService service;

    @BeforeEach
    void setUp() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldNotifyDefendantWhenEligibleWithStandardScenario() {
        CaseData caseData = new CaseDataBuilder()
            .respondent1Represented(YesOrNo.NO)
            .setMultiTrackClaim()
            .caseReference(4321L)
            .build();

        service.notifyDecisionOutcome(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("4321", "DEFENDANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("4321", "DEFENDANT");
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DEFENDANT_DECISION_OUTCOME.getScenario(),
            "4321",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldUseTrialReadyScenarioWhenSmallClaim() {
        CaseData caseData = new CaseDataBuilder()
            .setSmallTrackClaim()
            .atStateTrialReadyRespondent1()
            .respondent1Represented(YesOrNo.NO)
            .build();

        service.notifyDecisionOutcome(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DEFENDANT_TRIAL_READY_DECISION_OUTCOME.getScenario(),
            "1594901956117591",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldUseTrialReadyScenarioWhenRespondentMarkedTrialReady() {
        CaseData caseData = new CaseDataBuilder()
            .setMultiTrackClaim()
            .atStateTrialReadyRespondent1()
            .caseReference(2109L)
            .respondent1Represented(YesOrNo.NO)
            .build();

        service.notifyDecisionOutcome(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DEFENDANT_TRIAL_READY_DECISION_OUTCOME.getScenario(),
            "2109",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }
}
