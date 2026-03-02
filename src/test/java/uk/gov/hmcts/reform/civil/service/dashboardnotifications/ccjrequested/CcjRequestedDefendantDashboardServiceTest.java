package uk.gov.hmcts.reform.civil.service.dashboardnotifications.ccjrequested;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_DEF_PAYMENT_MISSED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_NO_DEF_RESPONSE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class CcjRequestedDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CcjRequestedDefendantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordNoResponseScenarioWhenDefendantRejectedSettlementAgreement() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setApplicant1SignedSettlementAgreement(YesOrNo.YES);

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        caseDataLiP.setRespondentSignSettlementAgreement(YesOrNo.NO);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_NO_DEF_RESPONSE_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordPaymentMissedScenarioWhenDefendantAcceptedSettlementAgreement() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setApplicant1SignedSettlementAgreement(YesOrNo.YES);

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondentSignSettlementAgreement(YesOrNo.YES);
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);

        RespondToClaimAdmitPartLRspec respondToClaim = new RespondToClaimAdmitPartLRspec(LocalDate.now().minusDays(1));

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaim);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_DEF_PAYMENT_MISSED_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordDefaultScenarioWhenNoSettlementAgreementData() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordNoResponseScenarioWhenDeadlineExpired() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setApplicant1SignedSettlementAgreement(YesOrNo.YES);

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setRespondent1RespondToSettlementAgreementDeadline(LocalDate.now().minusDays(1).atStartOfDay());

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_NO_DEF_RESPONSE_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordWhenToggleDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordDefaultScenarioWhenSettlementAgreementNotRespondedAndDeadlineNotExpired() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setApplicant1SignedSettlementAgreement(YesOrNo.YES);

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setRespondent1RespondToSettlementAgreementDeadline(LocalDate.now().plusDays(1).atStartOfDay());

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }
}
