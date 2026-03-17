package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_CLAIMANT_REJECTS_DEF_PLAN_CLAIMANT_DISAGREES_COURT_PLAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth";

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
    private ClaimantResponseDefendantDashboardService service;

    @BeforeEach
    void setUp() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioForMultiTrackAwaitingIntention() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(AllocatedTrack.INTERMEDIATE_CLAIM.name());
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioAndCleanupWhenCaseSettled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES);
        caseData.setCcdState(CaseState.CASE_SETTLED);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "DEFENDANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("1234", "DEFENDANT");
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordGeneralApplicationScenarioWhenProceedInHeritage() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordWhenToggleDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES);
        caseData.setCcdState(CaseState.CASE_SETTLED);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordWhenRespondentRepresented() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES);
        caseData.setCcdState(CaseState.CASE_SETTLED);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldRecordScenarioForPartAdmitImmediatePaymentClaimSettled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForLrvLipFullAdmitImmediatePaymentClaimSettled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarioWhenTrackNotMultiOrIntermediate() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        Party respondent = new Party();
        respondent.setType(Party.Type.INDIVIDUAL);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1(respondent);
        caseData.setResponseClaimTrack(AllocatedTrack.FAST_CLAIM.name());
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldRecordScenarioForRejectedCourtDecision() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse response = new ClaimantLiPResponse();
        response.setClaimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_PLAN);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(response);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_CLAIMANT_REJECTS_DEF_PLAN_CLAIMANT_DISAGREES_COURT_PLAN_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioWhenCourtDecisionInFavourOfDefendant() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse response = new ClaimantLiPResponse();
        response.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(response);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioWhenSettlementAgreementAndCourtDecisionInFavourOfClaimant() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse response = new ClaimantLiPResponse();
        response.setApplicant1SignedSettlementAgreement(YesOrNo.YES);
        response.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(response);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioWhenSettlementAgreementAndAcceptedRepaymentPlan() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse response = new ClaimantLiPResponse();
        response.setApplicant1SignedSettlementAgreement(YesOrNo.YES);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(response);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForJudicialReferralNotPaid() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForJudicialReferralStatesPaid() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(BigDecimal.ONE);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setRespondToClaim(respondToClaim);
        caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForJudicialReferralFullDefenceDisputes() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setApplicant1FullDefenceConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForJudicialReferralWhenMediationRequiredIsNull() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setApplicant1FullDefenceConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES);
        caseData.setResponseClaimMediationSpecRequired(null);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForJudicialReferralFullDefenceNoMediation() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantMediationLip mediationLip = new ClaimantMediationLip();
        mediationLip.setHasAgreedFreeMediation(MediationDecision.No);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1ClaimMediationSpecRequiredLip(mediationLip);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForJudicialReferralWhenClaimantMediationMissing() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseDataLiP caseDataLiP = new CaseDataLiP();

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setApplicant1FullDefenceConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForJudicialReferralPartAdmitRejectsClaimAmount() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForMediationCarmEnabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.IN_MEDIATION);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT_CARM.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForMediationCarmDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.IN_MEDIATION);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForClaimantRejectRepaymentPlan() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        Party respondent = new Party();
        respondent.setType(Party.Type.COMPANY);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);
        caseData.setRespondent1(respondent);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForClaimantRejectRepaymentPlanWhenLrvLip() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        Party respondent = new Party();
        respondent.setType(Party.Type.INDIVIDUAL);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1(respondent);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForLrvLipPartFullAdmitAndPayByPlan() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);

        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);
        caseData.setCcjPaymentDetails(ccjPaymentDetails);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForLrvLipFullDefenceNotProceed() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.NO);
        caseData.setDefenceRouteRequired(HAS_PAID_THE_AMOUNT_CLAIMED);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioWhenClaimantEndsClaim() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.CASE_STAYED);
        caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.NO);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarioWhenCaseSettledWithoutIntention() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.CASE_SETTLED);
        caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(null);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenJudicialReferralHasNoMatchingScenario() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(null);
        caseData.setRespondent1ClaimResponseTypeForSpec(null);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenNoScenarioMatches() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(null);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(null);
        caseData.setRespondent1ClaimResponseTypeForSpec(null);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenCaseSettledButClaimantDidNotSettlePartAdmit() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO);
        caseData.setCcdState(CaseState.CASE_SETTLED);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldRecordScenarioForJudicialReferralNotPaidWhenFullDefenceAndNoConfirmation() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setApplicant1FullDefenceConfirmAmountPaidSpec(YesOrNo.NO);
        caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(null);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForJudicialReferralStatesPaidWhenClaimantRejectsMediation() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(BigDecimal.ONE);

        ClaimantMediationLip mediationLip = new ClaimantMediationLip();
        mediationLip.setHasAgreedFreeMediation(MediationDecision.No);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1ClaimMediationSpecRequiredLip(mediationLip);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setRespondToClaim(respondToClaim);
        caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForJudicialReferralPartAdmitRejectsClaimantMediation() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantMediationLip mediationLip = new ClaimantMediationLip();
        mediationLip.setHasAgreedFreeMediation(MediationDecision.No);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1ClaimMediationSpecRequiredLip(mediationLip);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordJudicialReferralScenarioWhenNotFullDefenceForMediationCheck() {
        ClaimantMediationLip mediationLip = new ClaimantMediationLip();
        mediationLip.setHasAgreedFreeMediation(MediationDecision.No);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1ClaimMediationSpecRequiredLip(mediationLip);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordJudicialReferralScenarioWhenClaimantAgreedMediation() {
        ClaimantMediationLip mediationLip = new ClaimantMediationLip();
        mediationLip.setHasAgreedFreeMediation(MediationDecision.Yes);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1ClaimMediationSpecRequiredLip(mediationLip);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setApplicant1FullDefenceConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioForMultiTrackWhenNotAwaitingIntention() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(AllocatedTrack.INTERMEDIATE_CLAIM.name());
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenClaimantProceedsAfterCaseStayed() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.CASE_STAYED);
        caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.YES);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenRejectRepaymentPlanNotCompanyOrLrvLip() {
        Party respondent = new Party();
        respondent.setType(Party.Type.INDIVIDUAL);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1(respondent);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenCompanyAcceptedPlan() {
        Party respondent = new Party();
        respondent.setType(Party.Type.COMPANY);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1(respondent);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenJudgmentOnlineEnabledForLrvLipPlan() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setCcjPaymentDetails(ccjPaymentDetails);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenApplicantLipForLrvLipPlan() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setCcjPaymentDetails(ccjPaymentDetails);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenCcjRequestMissingForLrvLipPlan() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setCcjPaymentDetails(null);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenApplicantLipForFullDefenceNotProceed() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.NO);
        caseData.setDefenceRouteRequired(HAS_PAID_THE_AMOUNT_CLAIMED);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenNotFullDefenceForFullDefenceNotProceed() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.NO);
        caseData.setDefenceRouteRequired(HAS_PAID_THE_AMOUNT_CLAIMED);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenDefenceRouteNotPaidForFullDefenceNotProceed() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.NO);
        caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenJudgmentOnlineDisabledForFullAdmitImmediate() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setApplicant1ProceedWithClaim(null);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenApplicantLipForFullAdmitImmediate() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setApplicant1ProceedWithClaim(null);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenNotImmediatePayForFullAdmitImmediate() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        Party respondent = new Party();
        respondent.setType(Party.Type.INDIVIDUAL);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1(respondent);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1ProceedWithClaim(null);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }
}
