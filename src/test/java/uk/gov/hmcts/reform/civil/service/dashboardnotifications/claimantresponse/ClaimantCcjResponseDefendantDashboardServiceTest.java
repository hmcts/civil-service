package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_COURT_AGREE_WITH_CLAIMANT_CCJ_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_REJECT_PLAN_COURT_FAVOURS_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class ClaimantCcjResponseDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ClaimantCcjResponseDefendantDashboardService service;

    @BeforeEach
    void setUp() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioWhenAcceptedPlanAndCcjRequested() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse claimantResponse = new ClaimantLiPResponse();
        claimantResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioWhenCourtFavoursClaimantAndJbaRequested() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);

        ClaimantLiPResponse claimantResponse = new ClaimantLiPResponse();
        claimantResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcjPaymentDetails(ccjPaymentDetails);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_COURT_AGREE_WITH_CLAIMANT_CCJ_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioWhenCourtFavoursDefendantAndDecisionAccepted() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse claimantResponse = new ClaimantLiPResponse();
        claimantResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT);
        claimantResponse.setClaimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.ACCEPT_REPAYMENT_PLAN);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_REJECT_PLAN_COURT_FAVOURS_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordWhenCourtDecisionInFavourOfDefendantNotAccepted() {
        ClaimantLiPResponse claimantResponse = new ClaimantLiPResponse();
        claimantResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT);
        claimantResponse.setClaimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_PLAN);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenIneligible() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        ClaimantLiPResponse claimantResponse = new ClaimantLiPResponse();
        claimantResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarioWhenNoCcjDecisionMatches() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(null);
        caseData.setApplicant1AcceptPartAdmitPaymentPlanSpec(null);
        caseData.setCaseDataLiP(null);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenNoCcjRequestPresent() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setCaseDataLiP(null);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenCcjNotRequestedDespiteAcceptedPlan() {
        ClaimantLiPResponse claimantResponse = new ClaimantLiPResponse();
        claimantResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.SIGN_A_SETTLEMENT_AGREEMENT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenCourtDecisionInFavourOfClaimantButNoCcjRequest() {
        ClaimantLiPResponse claimantResponse = new ClaimantLiPResponse();
        claimantResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setCcjPaymentDetails(null);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenRespondentIsRepresented() {
        ClaimantLiPResponse claimantResponse = new ClaimantLiPResponse();
        claimantResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
