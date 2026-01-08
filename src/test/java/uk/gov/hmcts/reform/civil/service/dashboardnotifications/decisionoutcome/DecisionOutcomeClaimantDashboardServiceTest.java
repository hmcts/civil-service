package uk.gov.hmcts.reform.civil.service.dashboardnotifications.decisionoutcome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_TRIAL_READY_DECISION_OUTCOME;

@ExtendWith(MockitoExtension.class)
class DecisionOutcomeClaimantDashboardServiceTest {

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
    private DecisionOutcomeClaimantDashboardService service;

    @BeforeEach
    void setUp() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldNotifyClaimantWhenEligibleWithStandardScenario() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
            .ccdCaseReference(1234L)
            .build();

        service.notifyDecisionOutcome(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "CLAIMANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("1234", "CLAIMANT");
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIMANT_DECISION_OUTCOME.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldUseTrialReadyScenarioWhenSmallClaim() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .ccdCaseReference(5678L)
            .build();

        service.notifyDecisionOutcome(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIMANT_TRIAL_READY_DECISION_OUTCOME.getScenario(),
            "5678",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldUseTrialReadyScenarioWhenApplicantMarkedTrialReady() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
            .trialReadyApplicant(YesOrNo.YES)
            .ccdCaseReference(9012L)
            .build();

        service.notifyDecisionOutcome(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIMANT_TRIAL_READY_DECISION_OUTCOME.getScenario(),
            "9012",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }
}
