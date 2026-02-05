package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MESSAGES_TO_THE_COURT;

@ExtendWith(MockitoExtension.class)
class ClaimIssueDefendantDashboardServiceTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ClaimIssueDefendantDashboardService service;

    private static final String AUTH_TOKEN = "BEARER_TOKEN";

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarios_whenSmallClaimAndUnrepresented() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = new CaseDataBuilder()
            .ccdCaseReference(123L)
            .respondent1Represented(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(1000))
            .build();

        service.notifyClaimIssue(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED.getScenario(),
            "123",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verifyNoMoreInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordScenarios_whenFastTrackAndUnrepresented() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = new CaseDataBuilder()
            .ccdCaseReference(123L)
            .respondent1Represented(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(10001))
            .build();

        service.notifyClaimIssue(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED.getScenario(),
            "123",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_DEFENDANT.getScenario(),
            "123",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verifyNoMoreInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordScenarios_whenQueryManagementEnabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isLipQueryManagementEnabled(any())).thenReturn(true);
        CaseData caseData = new CaseDataBuilder()
            .ccdCaseReference(123L)
            .respondent1Represented(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(10001))
            .build();

        service.notifyClaimIssue(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED.getScenario(),
            "123",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_DEFENDANT.getScenario(),
            "123",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(),
            "123",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(),
            "123",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verifyNoMoreInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarios_whenRepresented() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = new CaseDataBuilder()
            .ccdCaseReference(123L)
            .respondent1Represented(YesOrNo.YES)
            .totalClaimAmount(new BigDecimal(1000))
            .build();

        service.notifyClaimIssue(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarios_whenFeatureToggleDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        CaseData caseData = new CaseDataBuilder()
            .ccdCaseReference(123L)
            .respondent1Represented(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(1000))
            .build();

        service.notifyClaimIssue(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordQueryScenarios_whenRespondentRepresentedAndQueryManagementEnabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isLipQueryManagementEnabled(any())).thenReturn(true);
        CaseData caseData = new CaseDataBuilder()
            .ccdCaseReference(123L)
            .respondent1Represented(YesOrNo.YES)
            .totalClaimAmount(new BigDecimal(1000))
            .build();

        service.notifyClaimIssue(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(),
            "123",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(),
            "123",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verifyNoMoreInteractions(dashboardScenariosService);
    }
}
