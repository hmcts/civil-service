package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ClaimantResponseDefendantNotificationHandler handler;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    public static final String TASK_ID = "GenerateDefendantDashboardNotificationClaimantResponse";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void configureDashboardNotificationsForClaimantResponse() {

        HashMap<String, Object> params = new HashMap<>();
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.CASE_SETTLED);
        caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES);
        caseData.setSpecRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT"
        );

        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT"
        );
    }

    @Test
    void configureDashboardNotificationsForClaimantResponseForPartAdmitImmediately() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1ClaimResponseTypeForSpec(PART_ADMISSION);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES);
        caseData.setApplicant1AcceptPartAdmitPaymentPlanSpec(null);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantNoMediationFullDefenceClaimantProceed() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotifications_courtFavoursClaimantSignedSettlementAgreement() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setApplicant1SignedSettlementAgreement(YesOrNo.YES);
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT);

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setSpecRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForClaimantResponseClaimSettleCourtDecisionFavorOfDefendant() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT);

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setSpecRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantPartAdmitHearing() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantFullDefenceClaimantProceed() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotifications_fullDefenceJudicialReferralClaimantAcceptsPartialAmountPaid() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(new BigDecimal("3000"));

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);
        caseData.setRespondent1ClaimResponseTypeForSpec(FULL_DEFENCE);
        caseData.setRespondToClaim(respondToClaim);
        caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
        caseData.setSpecRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotifications_partAdmitJudicialReferralClaimantAcceptsPartialAmountPaid() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        RespondToClaim respondToAdmittedClaim = new RespondToClaim();
        respondToAdmittedClaim.setHowMuchWasPaid(new BigDecimal("3000"));

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);
        caseData.setRespondent1ClaimResponseTypeForSpec(PART_ADMISSION);
        caseData.setRespondToAdmittedClaim(respondToAdmittedClaim);
        caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
        caseData.setSpecRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantPartAdmitNotPaidAmount() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);
        caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantFullDefenceNotPaidAmount() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(4321L);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setApplicant1FullDefenceConfirmAmountPaidSpec(YesOrNo.NO);
        caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantFullDefenceClaimantRejectMediation() {

        HashMap<String, Object> params = new HashMap<>();
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantMediationLip claimantMediationLip = new ClaimantMediationLip();
        claimantMediationLip.setHasAgreedFreeMediation(MediationDecision.No);

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1ClaimMediationSpecRequiredLip(claimantMediationLip);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(FULL_DEFENCE);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantMediation() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.IN_MEDIATION);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateNotificationForDefendantWhenClaimantRejectsRepaymentAndCourtPlan() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setClaimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE);

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1236345L);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_CLAIMANT_REJECTS_DEF_PLAN_CLAIMANT_DISAGREES_COURT_PLAN_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateNotificationForDefendantWhenClaimantRejectRepaymentPlanForFullAdmit() {
        // Given
        HashMap<String, Object> params = new HashMap<>();
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        Party applicant1 = new Party();
        applicant1.setOrganisationName("Applicant Org");
        applicant1.setType(Party.Type.ORGANISATION);

        Party respondent1 = new Party();
        respondent1.setOrganisationName("Org one");
        respondent1.setType(Party.Type.ORGANISATION);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(87654231L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1(applicant1);
        caseData.setRespondent1(respondent1);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setRespondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000));

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
        // When
        handler.handle(callbackParams);

        // Then
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @ParameterizedTest
    @MethodSource("provideCaseDataForRejectedPaymentPlan")
    void shouldCreateNotificationForDefendantWhenClaimantRejectRepaymentPlanForPartAdmit(CaseData caseData, int numberOfTimesApiCalled) {
        // Given
        HashMap<String, Object> params = new HashMap<>();
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
        // When
        handler.handle(callbackParams);

        // Then
        verify(dashboardScenariosService, times(numberOfTimesApiCalled)).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateNotificationForDefendantWhenClaimantProceedsCarm() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.IN_MEDIATION);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT_CARM.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateNotificationForDefendantWhenClaimantLrAcceptPaymentPlan() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.NO);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1000001L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setCcjPaymentDetails(ccjPaymentDetails);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldReturnNullWhenNothingIsMatched_Applicant1RejectPaymentPlan() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1000001L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnNullWhenNothingIsMatched_Applicant1NotRepresented() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1000002L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnNullWhenNothingIsMatched_JudgmentOnlineIsSet() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1000003L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldCreateNotificationForDefendantWhenClaimantLrNotProceedTheClaim() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(10000006L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(FULL_DEFENCE);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.NO);
        caseData.setDefenceRouteRequired(HAS_PAID_THE_AMOUNT_CLAIMED);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateNotificationForDefendantWhenClaimantProceedTheClaimMultiTrack() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(10000006L);
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(FULL_DEFENCE);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.NO);
        caseData.setDefenceRouteRequired(HAS_PAID_THE_AMOUNT_CLAIMED);
        caseData.setResponseClaimTrack(AllocatedTrack.MULTI_CLAIM.name());

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateNotificationForDefendantWhenClaimantProceedTheClaimIntTrack() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(10000006L);
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(FULL_DEFENCE);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.NO);
        caseData.setDefenceRouteRequired(HAS_PAID_THE_AMOUNT_CLAIMED);
        caseData.setResponseClaimTrack(AllocatedTrack.INTERMEDIATE_CLAIM.name());

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldReturnNullWhenNothingIsMatched_ClaimantProceedWithClaim() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(10000006L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(FULL_DEFENCE);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.YES);
        caseData.setDefenceRouteRequired(HAS_PAID_THE_AMOUNT_CLAIMED);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnNullWhenNothingIsMatched_DisputeClaimIsSelected() {
        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(10000006L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(FULL_DEFENCE);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.NO);
        caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        assertThat(response.getErrors()).isNull();
    }

    private static Stream<Arguments> provideCaseDataForRejectedPaymentPlan() {
        Party applicant1Company = new Party();
        applicant1Company.setCompanyName("COMPANY");
        applicant1Company.setType(Party.Type.COMPANY);

        Party respondent1Individual = new Party();
        respondent1Individual.setPartyName("Respondent");
        respondent1Individual.setType(Party.Type.INDIVIDUAL);

        Party respondent1Company = new Party();
        respondent1Company.setCompanyName("COMPANY one");
        respondent1Company.setType(Party.Type.COMPANY);

        Party respondent1IndividualWithCompanyName = new Party();
        respondent1IndividualWithCompanyName.setCompanyName("COMPANY one");
        respondent1IndividualWithCompanyName.setType(Party.Type.INDIVIDUAL);

        RepaymentPlanLRspec repaymentPlan = new RepaymentPlanLRspec()
            .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
            .setPaymentAmount(new BigDecimal(1000))
            .setFirstRepaymentDate(LocalDate.now());

        // Base case data setup
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(78034251L);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1(applicant1Company);
        caseData.setRespondent1(respondent1Individual);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);
        caseData.setRespondent1RepaymentPlan(repaymentPlan);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setRespondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000));

        // respondentAsCompany
        CaseData respondentAsCompany = CaseDataBuilder.builder().build();
        respondentAsCompany.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        respondentAsCompany.setLegacyCaseReference("reference");
        respondentAsCompany.setCcdCaseReference(78034251L);
        respondentAsCompany.setApplicant1Represented(YesOrNo.YES);
        respondentAsCompany.setRespondent1Represented(YesOrNo.NO);
        respondentAsCompany.setApplicant1(applicant1Company);
        respondentAsCompany.setRespondent1(respondent1Company);
        respondentAsCompany.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);
        respondentAsCompany.setRespondent1RepaymentPlan(repaymentPlan);
        respondentAsCompany.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
        respondentAsCompany.setRespondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000));

        // applicantAsRepresented
        CaseData applicantAsRepresented = CaseDataBuilder.builder().build();
        applicantAsRepresented.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        applicantAsRepresented.setLegacyCaseReference("reference");
        applicantAsRepresented.setCcdCaseReference(78034251L);
        applicantAsRepresented.setApplicant1Represented(YesOrNo.YES);
        applicantAsRepresented.setRespondent1Represented(YesOrNo.NO);
        applicantAsRepresented.setApplicant1(applicant1Company);
        applicantAsRepresented.setRespondent1(respondent1Individual);
        applicantAsRepresented.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);
        applicantAsRepresented.setRespondent1RepaymentPlan(repaymentPlan);
        applicantAsRepresented.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
        applicantAsRepresented.setRespondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000));

        // lipApplicantIndividualRespondent
        CaseData lipApplicantIndividualRespondent = CaseDataBuilder.builder().build();
        lipApplicantIndividualRespondent.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        lipApplicantIndividualRespondent.setLegacyCaseReference("reference");
        lipApplicantIndividualRespondent.setCcdCaseReference(78034251L);
        lipApplicantIndividualRespondent.setApplicant1Represented(YesOrNo.NO);
        lipApplicantIndividualRespondent.setRespondent1Represented(YesOrNo.NO);
        lipApplicantIndividualRespondent.setApplicant1(applicant1Company);
        lipApplicantIndividualRespondent.setRespondent1(respondent1IndividualWithCompanyName);
        lipApplicantIndividualRespondent.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);
        lipApplicantIndividualRespondent.setRespondent1RepaymentPlan(repaymentPlan);
        lipApplicantIndividualRespondent.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
        lipApplicantIndividualRespondent.setRespondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000));

        // applicantAcceptedRepaymentPlan
        CaseData applicantAcceptedRepaymentPlan = CaseDataBuilder.builder().build();
        applicantAcceptedRepaymentPlan.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        applicantAcceptedRepaymentPlan.setLegacyCaseReference("reference");
        applicantAcceptedRepaymentPlan.setCcdCaseReference(78034251L);
        applicantAcceptedRepaymentPlan.setApplicant1Represented(YesOrNo.YES);
        applicantAcceptedRepaymentPlan.setRespondent1Represented(YesOrNo.NO);
        applicantAcceptedRepaymentPlan.setApplicant1(applicant1Company);
        applicantAcceptedRepaymentPlan.setRespondent1(respondent1Individual);
        applicantAcceptedRepaymentPlan.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        applicantAcceptedRepaymentPlan.setRespondent1RepaymentPlan(repaymentPlan);
        applicantAcceptedRepaymentPlan.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
        applicantAcceptedRepaymentPlan.setRespondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000));

        return Stream.of(
            Arguments.of(respondentAsCompany, 1),
            Arguments.of(applicantAsRepresented, 1),
            Arguments.of(lipApplicantIndividualRespondent, 0),
            Arguments.of(applicantAcceptedRepaymentPlan, 0)
        );
    }

    @Test
    void configureDashboardNotificationsForFullDisputeFullDefenceCaseStayed() {

        HashMap<String, Object> params = new HashMap<>();
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.CASE_STAYED);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.NO);
        caseData.setSpecRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(FULL_DEFENCE);
        caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);
        caseData.setRespondent1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendant_WhenLRvLipFullAdmitPayImmediately() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YES);
        caseData.setApplicant1ProceedWithClaim(null);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }
}
