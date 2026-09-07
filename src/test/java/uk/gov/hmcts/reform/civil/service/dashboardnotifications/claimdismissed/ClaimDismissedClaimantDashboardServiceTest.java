package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimdismissed;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DISMISS_CASE_CCJ_CANCELLED_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class ClaimDismissedClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private uk.gov.hmcts.reform.civil.service.FeatureToggleService toggleService;

    @InjectMocks
    private ClaimDismissedClaimantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordCcjCancelledScenarioWhenJudgementBufferEnabledAndJoRequested() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .ccdCaseReference(1234L)
            .isJoRequested(YesOrNo.YES)
            .build();
        when(toggleService.isJudgmentBufferEnabled()).thenReturn(true);

        service.notifyClaimDismissed(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DISMISS_CASE_CCJ_CANCELLED_CLAIMANT.getScenario(),
            "1234",
            new ScenarioRequestParams(new HashMap<>())
        );
    }

    @Test
    void shouldNotRecordCcjCancelledScenarioWhenJudgementBufferEnabledAndJoNotRequested() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .ccdCaseReference(1234L)
            .isJoRequested(null)
            .build();
        when(toggleService.isJudgmentBufferEnabled()).thenReturn(true);

        service.notifyClaimDismissed(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DISMISS_CASE_CCJ_CANCELLED_CLAIMANT.getScenario(),
            "1234",
            new ScenarioRequestParams(new HashMap<>())
        );
    }

    @Test
    void shouldNotRecordCcjCancelledScenarioWhenJudgementBufferDisabled() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .ccdCaseReference(1234L)
            .isJoRequested(YesOrNo.YES)
            .build();
        when(toggleService.isJudgmentBufferEnabled()).thenReturn(false);

        service.notifyClaimDismissed(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DISMISS_CASE_CCJ_CANCELLED_CLAIMANT.getScenario(),
            "1234",
            new ScenarioRequestParams(new HashMap<>())
        );
    }
}
