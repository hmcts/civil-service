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

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_DEFENDANT_ADMIT_PAY_IMMEDIATELY_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_DEFENDANT;

@Service
@RequiredArgsConstructor
public class DefendantResponseDefendantNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE);
    public static final String TASK_ID = "GenerateDefendantDashboardNotificationDefendantResponse";
    private final DashboardApiClient dashboardApiClient;
    private final DashboardNotificationsParamsMapper mapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::configureScenarioForDefendantResponse
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

        if (caseData.getRespondent1().isCompanyOROrganisation()
            && caseData.isPayBySetDate()) {
            return SCENARIO_AAA7_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_DEFENDANT.getScenario();
        }

        if (caseData.isFullAdmitPayImmediatelyClaimSpec()
            || caseData.isPartAdmitPayImmediatelyClaimSpec()) {
            return SCENARIO_AAA7_DEFENDANT_ADMIT_PAY_IMMEDIATELY_DEFENDANT.getScenario();
        }

        return null;
    }

    private CallbackResponse configureScenarioForDefendantResponse(CallbackParams callbackParams) {
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
