package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT;

@Service
@RequiredArgsConstructor
public class ClaimantResponseDefendantNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE);
    public static final String TASK_ID = "GenerateDefendantDashboardNotificationClaimantResponse";
    private final DashboardApiClient dashboardApiClient;
    private final DashboardNotificationsParamsMapper mapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::configureScenarioForClaimantResponse
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private String getScenario(CaseData caseData) {
        if (isCaseStateSettled(caseData)) {
            return getCaseSettledScenarios(caseData);
        } else if (caseData.hasApplicant1CourtDecisionInFavourOfDefendant()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT.getScenario();
        } else if (caseData.hasApplicant1SignedSettlementAgreement() && caseData.hasApplicant1CourtDecisionInFavourOfClaimant()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT.getScenario();
        } else if (caseData.hasApplicantAcceptedRepaymentPlan() && caseData.hasApplicant1SignedSettlementAgreement()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT.getScenario();
        } else if (isCaseStateJudicialReferral(caseData)) {
            return getJudicialReferralScenarios(caseData);
        } else if (isCaseStateInMediation(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT.getScenario();
        }
        return null;
    }

    private String getCaseSettledScenarios(CaseData caseData) {
        if (Objects.nonNull(caseData.getApplicant1PartAdmitIntentionToSettleClaimSpec()) && caseData.isClaimantIntentionSettlePartAdmit()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario();
        } else if (caseData.isPartAdmitImmediatePaymentClaimSettled()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario();
        }
        return null;
    }

    private String getJudicialReferralScenarios(CaseData caseData) {
        RespondToClaim respondToClaim = getRespondToClaim(caseData);
        if ((caseData.hasDefendantNotPaid()
            || (RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            && (caseData.isFullDefenceNotPaid() && caseData.isClaimantIntentionNotSettlePartAdmit()))
            && caseData.isMediationRejectedOrFastTrack())) {
            return SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT.getScenario();
        }
        if (Objects.nonNull(respondToClaim)
            && Objects.nonNull(respondToClaim.getHowMuchWasPaid())
            && caseData.getApplicant1PartAdmitConfirmAmountPaidSpec() == YesOrNo.YES
            && (caseData.hasClaimantNotAgreedToFreeMediation()
            || caseData.hasDefendantNotAgreedToFreeMediation())) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT
                .getScenario();
        }

        if (caseData.isRespondentResponseFullDefence()
            && (isNull(caseData.getResponseClaimMediationSpecRequired())
            || caseData.hasDefendantNotAgreedToFreeMediation())) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT
                .getScenario();
        }
        if (getGoToHearingScenarioClaimantRejectsMediation(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT
                .getScenario();
        }
        if (Objects.nonNull(caseData.getApplicant1AcceptAdmitAmountPaidSpec()) && caseData.isClaimantRejectsClaimAmount()
            && (caseData.hasClaimantNotAgreedToFreeMediation()
            || caseData.hasDefendantNotAgreedToFreeMediation())) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT.getScenario();
        }
        return null;
    }

    private static boolean isCaseStateSettled(CaseData caseData) {
        return caseData.getCcdState() == CASE_SETTLED;
    }

    private static boolean isCaseStateJudicialReferral(CaseData caseData) {
        return caseData.getCcdState() == JUDICIAL_REFERRAL;
    }

    private static boolean isCaseStateInMediation(CaseData caseData) {
        return caseData.getCcdState() == IN_MEDIATION;
    }

    private CallbackResponse configureScenarioForClaimantResponse(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String scenario = getScenario(caseData);
        if (!Strings.isNullOrEmpty(scenario) && caseData.isRespondent1NotRepresented()) {
            dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                scenario,
                authToken,
                ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(
                    caseData)).build()
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private RespondToClaim getRespondToClaim(CaseData caseData) {
        RespondToClaim respondToClaim = null;
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
            respondToClaim = caseData.getRespondToClaim();
        } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            respondToClaim = caseData.getRespondToAdmittedClaim();
        }

        return respondToClaim;
    }

    private boolean getGoToHearingScenarioClaimantRejectsMediation(CaseData caseData) {
        Optional<CaseDataLiP> caseDataLip = Optional.ofNullable(caseData.getCaseDataLiP());
        return caseDataLip.filter(caseDataLiP -> caseData.isRespondentResponseFullDefence()
            && (isNull(caseDataLiP.getApplicant1ClaimMediationSpecRequiredLip())
            || (caseDataLiP.getApplicant1ClaimMediationSpecRequiredLip()
            .getHasAgreedFreeMediation().equals(MediationDecision.No)))).isPresent();
    }

}
