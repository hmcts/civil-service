package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

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

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_DEFENDANT;

@Service
@RequiredArgsConstructor
public class ClaimantResponseNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE);
    public static final String TASK_ID = "GenerateClaimantDashboardNotificationClaimantResponse";
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

    private CallbackResponse configureScenarioForClaimantResponse(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        String scenario = getScenario(caseData);
        if (!Strings.isNullOrEmpty(scenario)) {
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

    private String getScenario(CaseData caseData) {
        if (caseData.getCcdState() == CASE_SETTLED) {
            if (caseData.isPartAdmitImmediatePaymentClaimSettled()) {
                return SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT.getScenario();
            }
            return SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT.getScenario();
        } else if (caseData.getCcdState() == JUDICIAL_REFERRAL) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING.getScenario();
        } else if (caseData.getCcdState() == IN_MEDIATION) {
            return SCENARIO_AAA6_CLAIMANT_MEDIATION.getScenario();
        } else if (caseData.hasApplicant1SignedSettlementAgreement()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT.getScenario();
        }
        if ((caseData.isPayBySetDate() || caseData.isPayByInstallment())
                && caseData.getRespondent1().isCompanyOROrganisation()
                && caseData.hasApplicantRejectedRepaymentPlan()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_DEFENDANT.getScenario();
        }
        return null;
    }
}
