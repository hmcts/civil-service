package uk.gov.hmcts.reform.civil.service.dashboardnotifications.djnondivergent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFISLIP_JUDGMENT_REQUESTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_GRANTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_ISSUED_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class DjNonDivergentClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;

    @InjectMocks
    private DjNonDivergentClaimantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioWhenClaimantIsLip() {
        CaseData caseData = claimantLipCaseData();

        service.notifyDjNonDivergent(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_ISSUED_CLAIMANT.getScenario(),
            "1594901956117591",
            new ScenarioRequestParams(new HashMap<>())
        );
    }

    @Test
    void shouldRecordJudgmentRequestedScenarioWhenBufferEnabledAndCaseInJudgmentRequestedState() {
        when(toggleService.isJudgmentBufferEnabled()).thenReturn(true);

        CaseData caseData = claimantLipCaseData();
        caseData.setCcdState(CaseState.JUDGMENT_REQUESTED);

        service.notifyDjNonDivergent(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DEFISLIP_JUDGMENT_REQUESTED_CLAIMANT.getScenario(),
            "1594901956117591",
            new ScenarioRequestParams(new HashMap<>())
        );
    }

    @Test
    void shouldRecordDefaultScenarioWhenBufferDisabledAndCaseInJudgmentRequestedState() {
        when(toggleService.isJudgmentBufferEnabled()).thenReturn(false);

        CaseData caseData = claimantLipCaseData();
        caseData.setCcdState(CaseState.JUDGMENT_REQUESTED);

        service.notifyDjNonDivergent(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_ISSUED_CLAIMANT.getScenario(),
            "1594901956117591",
            new ScenarioRequestParams(new HashMap<>())
        );
    }

    @Test
    void shouldRecordEnteredScenarioWhenClaimantIsLipAndJudgmentBufferEnabled() {
        when(toggleService.isJudgmentBufferEnabled()).thenReturn(true);
        CaseData caseData = claimantLipFinalOrdersIssuedDefaultJudgmentCaseData();

        service.notifyDjNonDivergent(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_GRANTED_CLAIMANT.getScenario(),
            "1594901956117591",
            new ScenarioRequestParams(new HashMap<>())
        );
    }

    @Test
    void shouldRecordIssuedScenarioWhenJudgmentBufferEnabledAndJudgmentNotIssued() {
        when(toggleService.isJudgmentBufferEnabled()).thenReturn(true);
        CaseData caseData = claimantLipCaseData();

        service.notifyDjNonDivergent(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_ISSUED_CLAIMANT.getScenario(),
            "1594901956117591",
            new ScenarioRequestParams(new HashMap<>())
        );
    }

    private CaseData claimantLipCaseData() {
        return new CaseDataBuilder()
            .applicant1Represented(YesOrNo.NO)
            .atStateClaimIssued()
            .build();
    }

    private CaseData claimantLipFinalOrdersIssuedDefaultJudgmentCaseData() {
        return claimantLipCaseData().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .activeJudgment(defaultJudgmentIssued())
            .build();
    }

    private JudgmentDetails defaultJudgmentIssued() {
        return new JudgmentDetails()
            .setType(JudgmentType.DEFAULT_JUDGMENT)
            .setState(JudgmentState.ISSUED);
    }
}
