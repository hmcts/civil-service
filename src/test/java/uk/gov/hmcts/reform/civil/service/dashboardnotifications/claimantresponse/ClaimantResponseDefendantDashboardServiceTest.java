package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
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
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_CLAIMANT_REJECTS_DEF_PLAN_CLAIMANT_DISAGREES_COURT_PLAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER_TOKEN";
    private static final String CASE_REFERENCE = "1234";

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
    void setup() {
        service = new ClaimantResponseDefendantDashboardService(
            dashboardScenariosService,
            mapper,
            featureToggleService,
            dashboardNotificationService,
            taskListService
        );
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    private CaseData.CaseDataBuilder<?, ?> baseCaseDataBuilder() {
        return CaseData.builder()
            .ccdCaseReference(Long.valueOf(CASE_REFERENCE))
            .respondent1Represented(YesOrNo.NO)
            .respondent1(Party.builder().type(Party.Type.COMPANY).build());
    }

    @Test
    void shouldRecordCaseSettledScenarioAndClearDefendantTasks() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CASE_SETTLED)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(CASE_REFERENCE, "DEFENDANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(CASE_REFERENCE, "DEFENDANT");
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordCaseSettledScenarioWhenClaimantIntentionMissing() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CASE_SETTLED)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordCaseSettledScenarioWhenClaimantIntentionNo() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CASE_SETTLED)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldRecordGeneralApplicationScenarioWhenProceedingInHeritageSystem() {
        ClaimantLiPResponse response = ClaimantLiPResponse.builder()
            .claimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE)
            .build();
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1LiPResponse(response)
            .build();

        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_CLAIMANT_REJECTS_DEF_PLAN_CLAIMANT_DISAGREES_COURT_PLAN_DEFENDANT
                .getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
        verifyNoInteractions(dashboardNotificationService, taskListService);
    }

    @Test
    void shouldSkipScenarioWhenRespondentRepresented() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CASE_SETTLED)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @ParameterizedTest
    @EnumSource(value = AllocatedTrack.class, names = {"MULTI_CLAIM", "INTERMEDIATE_CLAIM"})
    void shouldRecordMintiScenarioWhenAwaitingApplicantIntention(AllocatedTrack track) {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .responseClaimTrack(track.name())
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordMintiScenarioWhenToggleDisabled() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .responseClaimTrack(AllocatedTrack.MULTI_CLAIM.name())
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordMintiScenarioWhenTrackNotEligible() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordMintiScenarioWhenNotAwaitingApplicantIntention() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .responseClaimTrack(AllocatedTrack.MULTI_CLAIM.name())
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordPartAdmitImmediatePaymentScenario() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordFullAdmitImmediatePaymentScenarioWhenJudgmentOnlineLive() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordFullAdmitImmediatePaymentScenarioWhenJudgmentOnlineDisabled() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordFullAdmitImmediatePaymentScenarioWhenApplicantLiP() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordFullAdmitImmediatePaymentScenarioWhenNotPayImmediately() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES)
            .build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordFullAdmitImmediatePaymentScenarioWhenApplicantProceedWithClaimSet() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordCourtDecisionInFavourOfDefendantScenario() {
        ClaimantLiPResponse response = ClaimantLiPResponse.builder()
            .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT)
            .build();
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1LiPResponse(response)
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordSettlementAgreementRejectedScenario() {
        ClaimantLiPResponse response = ClaimantLiPResponse.builder()
            .applicant1SignedSettlementAgreement(YesOrNo.YES)
            .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
            .build();
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1LiPResponse(response)
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT
                .getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordSettlementAgreementAcceptedScenario() {
        ClaimantLiPResponse response = ClaimantLiPResponse.builder()
            .applicant1SignedSettlementAgreement(YesOrNo.YES)
            .build();
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1LiPResponse(response)
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordJudicialReferralScenarioNotPaid() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordJudicialReferralScenarioFullDefenceNotPaid() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1FullDefenceConfirmAmountPaidSpec(YesOrNo.NO)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordJudicialReferralScenarioWhenClaimantIntentionNotSettlePartAdmit() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1FullDefenceConfirmAmountPaidSpec(YesOrNo.YES)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordJudicialReferralScenarioWhenFullDefencePaidConfirmationYes() {
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                .hasAgreedFreeMediation(MediationDecision.Yes)
                .build())
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1FullDefenceConfirmAmountPaidSpec(YesOrNo.NO)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordJudicialReferralScenarioStatesPaid() {
        RespondToClaim respondToClaim = RespondToClaim.builder()
            .howMuchWasPaid(java.math.BigDecimal.valueOf(1000))
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondToClaim(respondToClaim)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT
                .getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordJudicialReferralScenarioStatesPaidWhenClaimantNotAgreedToMediation() {
        RespondToClaim respondToClaim = RespondToClaim.builder()
            .howMuchWasPaid(java.math.BigDecimal.valueOf(1000))
            .build();
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                .hasAgreedFreeMediation(MediationDecision.No)
                .build())
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondToClaim(respondToClaim)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT
                .getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordJudicialReferralScenarioWhenRespondToClaimMissingAmount() {
        RespondToClaim respondToClaim = RespondToClaim.builder().build();
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                .hasAgreedFreeMediation(MediationDecision.Yes)
                .build())
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondToAdmittedClaim(respondToClaim)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordJudicialReferralScenarioWhenPaidAmountButClaimantConfirmationMissing() {
        RespondToClaim respondToClaim = RespondToClaim.builder()
            .howMuchWasPaid(java.math.BigDecimal.valueOf(1000))
            .build();
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                .hasAgreedFreeMediation(MediationDecision.Yes)
                .build())
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondToAdmittedClaim(respondToClaim)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordJudicialReferralScenarioWhenMediationAgreedAfterPaymentConfirmed() {
        RespondToClaim respondToClaim = RespondToClaim.builder()
            .howMuchWasPaid(java.math.BigDecimal.valueOf(1000))
            .build();
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                .hasAgreedFreeMediation(MediationDecision.Yes)
                .build())
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondToClaim(respondToClaim)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .applicant1FullDefenceConfirmAmountPaidSpec(YesOrNo.YES)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordJudicialReferralScenarioWhenMediationAccepted() {
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                .hasAgreedFreeMediation(MediationDecision.Yes)
                .build())
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1FullDefenceConfirmAmountPaidSpec(YesOrNo.NO)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordJudicialReferralScenarioWhenClaimantMediationLipMissing() {
        CaseDataLiP caseDataLiP = CaseDataLiP.builder().build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT
                .getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordJudicialReferralScenarioFullDefenceDisputes() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .responseClaimMediationSpecRequired(null)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordJudicialReferralScenarioFullDefenceDisputesWhenDefendantNotAgreedToMediation() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordJudicialReferralScenarioFullDefenceNoMediation() {
        ClaimantMediationLip mediationLip = ClaimantMediationLip.builder()
            .hasAgreedFreeMediation(MediationDecision.No)
            .build();
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(mediationLip)
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT
                .getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordJudicialReferralScenarioDefendantPartAdmit() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordJudicialReferralScenarioDefendantPartAdmitWhenClaimantNotAgreedToMediation() {
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                .hasAgreedFreeMediation(MediationDecision.No)
                .build())
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordJudicialReferralScenarioWhenClaimantAcceptsAmountPaid() {
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                .hasAgreedFreeMediation(MediationDecision.Yes)
                .build())
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordJudicialReferralScenarioWhenMediationAgreedAfterClaimantRejectsAmount() {
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                .hasAgreedFreeMediation(MediationDecision.Yes)
                .build())
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordInMediationScenarioWhenCarmEnabled() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.IN_MEDIATION)
            .build();
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT_CARM.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordInMediationScenarioWhenCarmDisabled() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.IN_MEDIATION)
            .build();
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordRejectRepaymentPlanScenario() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordRejectRepaymentPlanScenarioWhenPaymentTimeInstallment() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordRejectRepaymentPlanScenarioWhenApplicantLipButRespondentCompany() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordRejectRepaymentPlanScenarioWhenPaymentTimeNotPlan() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordRejectRepaymentPlanScenarioWhenNotLrVlipOrCompany() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordRejectRepaymentPlanScenarioWhenPlanAccepted() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordRequestedCcjScenarioWhenApplicantAcceptedPlan() {
        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
            .ccjPaymentPaidSomeOption(YesOrNo.YES)
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.YES)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .ccjPaymentDetails(ccjPaymentDetails)
            .build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordRequestedCcjScenarioWhenJudgmentOnlineLive() {
        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
            .ccjPaymentPaidSomeOption(YesOrNo.YES)
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.YES)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .ccjPaymentDetails(ccjPaymentDetails)
            .build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordRequestedCcjScenarioWhenApplicantLiP() {
        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
            .ccjPaymentPaidSomeOption(YesOrNo.YES)
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.NO)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .ccjPaymentDetails(ccjPaymentDetails)
            .build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordRequestedCcjScenarioWhenApplicantHasNotAcceptedPlan() {
        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
            .ccjPaymentPaidSomeOption(YesOrNo.YES)
            .build();
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.YES)
            .ccjPaymentDetails(ccjPaymentDetails)
            .build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordRequestedCcjScenarioWhenCcjRequestMissing() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.YES)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordFullDefenceNotProceedScenario() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordFullDefenceNotProceedScenarioWhenApplicantLiP() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordFullDefenceNotProceedScenarioWhenClaimantProceeds() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordFullDefenceNotProceedScenarioWhenDefenceRouteNotPaid() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .applicant1Represented(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordClaimantEndsClaimScenario() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_STAYED)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM)
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordClaimantEndsClaimScenarioWhenClaimantProceeds() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_STAYED)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM)
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
