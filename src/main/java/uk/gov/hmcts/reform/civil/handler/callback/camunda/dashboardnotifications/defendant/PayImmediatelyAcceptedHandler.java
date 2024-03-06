package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_PAY_IMMEDIATELY_ACCEPTED_FOR_RESPONDENT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayImmediatelyAcceptedHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_PAY_IMMEDIATELY_ACCEPTED_FOR_RESPONDENT1);
    public static final String TASK_ID = "PayImmediatelyAcceptedDashboardNotificationForDefendant1";

    private final DashboardApiClient dashboardApiClient;
    private final DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::createDashboardNotifications
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

    public CallbackResponse createDashboardNotifications(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        Map<String, Object> params = dashboardNotificationsParamsMapper.mapCaseDataToParams(caseData);

        dashboardApiClient.recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA7_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario(),
            authToken,
            ScenarioRequestParams.builder().params(params).build()
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
