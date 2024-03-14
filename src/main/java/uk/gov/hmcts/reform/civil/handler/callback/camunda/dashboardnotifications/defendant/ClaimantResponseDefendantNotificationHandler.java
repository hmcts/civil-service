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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT;

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
        if (caseData.getCcdState() == CASE_SETTLED) {
            if (Objects.nonNull(caseData.getApplicant1PartAdmitIntentionToSettleClaimSpec())
                && caseData.isClaimantIntentionSettlePartAdmit()) {
                return SCENARIO_AAA7_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario();
            } else if (caseData.isPartAdmitImmediatePaymentClaimSettled()) {
                return SCENARIO_AAA7_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario();
            }
        } else if (caseData.hasApplicant1SignedSettlementAgreement() && caseData.hasApplicant1CourtDecisionInFavourOfClaimant()) {
            return SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT
                .getScenario();
        }  else if (caseData.hasApplicantAcceptedRepaymentPlan() && caseData.hasApplicant1SignedSettlementAgreement()) {
            return SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT.getScenario();
        } else if (caseData.getCcdState() == JUDICIAL_REFERRAL) {
            if (caseData.isRespondentResponseFullDefence()
                && (isNull(caseData.getResponseClaimMediationSpecRequired())
                || caseData.hasDefendantNotAgreedToFreeMediation())) {
                return SCENARIO_AAA7_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT
                    .getScenario();
            }

            if (Objects.nonNull(caseData.getApplicant1AcceptAdmitAmountPaidSpec()) && caseData.isClaimantRejectsClaimAmount()
                    && (caseData.hasClaimantNotAgreedToFreeMediation()
                    || caseData.hasDefendantNotAgreedToFreeMediation())) {
                return SCENARIO_AAA7_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT.getScenario();
            }
        }

        return null;
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
}
