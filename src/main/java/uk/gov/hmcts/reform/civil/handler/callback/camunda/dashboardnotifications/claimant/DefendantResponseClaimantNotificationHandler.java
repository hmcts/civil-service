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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_ORG_COM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_ADMIT_PAY_IMMEDIATELY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_PART_ADMIT_PAY_IMMEDIATELY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MEDIATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEF_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_REFUSED_MEDIATION_CLAIMANT;

@Service
@RequiredArgsConstructor
public class DefendantResponseClaimantNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE);
    public static final String TASK_ID = "GenerateClaimantDashboardNotificationDefendantResponse";
    private final DashboardApiClient dashboardApiClient;
    private final DashboardNotificationsParamsMapper mapper;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return featureToggleService.isDashboardServiceEnabled()
            ? Map.of(callbackKey(ABOUT_TO_SUBMIT), this::configureScenarioForDefendantResponse)
            : Map.of(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse);
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
        if (caseData.isPaidLessThanClaimAmount()) {
            return SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT.getScenario();
        }

        if (isFullDefenseMediationRefusedScenario(caseData)) {
            return SCENARIO_AAA6_DEF_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_REFUSED_MEDIATION_CLAIMANT.getScenario();
        }

        if (caseData.isPayByInstallment()) {
            if (caseData.isPartAdmitClaimSpec() || caseData.isFullAdmitClaimSpec()) {
                if (caseData.getRespondent1().isCompanyOROrganisation()) {
                    return SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_ORG_COM_CLAIMANT.getScenario();
                } else {
                    return SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT.getScenario();
                }
            }
        }
        if (caseData.isPayBySetDate()) {
            if (caseData.getRespondent1().isCompanyOROrganisation()) {
                return SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT.getScenario();
            } else if (caseData.getRespondent1().isIndividualORSoleTrader()) {
                return SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT.getScenario();
            }
        }

        if (caseData.isPayImmediately()) {
            if (caseData.isFullAdmitClaimSpec()) {
                return SCENARIO_AAA6_DEFENDANT_FULL_ADMIT_PAY_IMMEDIATELY_CLAIMANT.getScenario();
            } else if (caseData.isPartAdmitClaimSpec()) {
                return SCENARIO_AAA6_DEFENDANT_PART_ADMIT_PAY_IMMEDIATELY_CLAIMANT.getScenario();
            }
        }

        if (caseData.isRespondentResponseFullDefence() && caseData.isClaimBeingDisputed()
            && caseData.hasDefendantAgreedToFreeMediation()) {
            return SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MEDIATION_CLAIMANT.getScenario();
        }

        return null;
    }

    private CallbackResponse configureScenarioForDefendantResponse(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String scenario = getScenario(caseData);
        if (!Strings.isNullOrEmpty(scenario) && caseData.isApplicant1NotRepresented()) {
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

    private boolean isFullDefenseMediationRefusedScenario(CaseData caseData) {
        return caseData.isRespondentResponseFullDefence() && caseData.hasDefendantNotAgreedToFreeMediation();
    }
}
