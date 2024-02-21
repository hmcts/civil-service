package uk.gov.hmcts.reform.civil.handler.callback.camunda.cuidashboard;

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
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DASHBOARD_NOTIFICATION_CUI;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.cuidashboard.DashboardScenarios.SCENARIO_AAA7_CLAIM_ISSUE_CLAIM_FEE_REQUIRED;

@Service
@RequiredArgsConstructor
public class GenerateDashboardNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(GENERATE_DASHBOARD_NOTIFICATION_CUI);
    public static final String TASK_ID = "GenerateDashboardNotificationCUI";
    private final DashboardApiClient dashboardApiClient;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::sendScenario,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
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

    private CallbackResponse sendScenario(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        Map<String, Object> params = new HashMap<>();
        params.put("claimFee", MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence()).toString());

        dashboardApiClient.recordScenario(caseData.getCcdCaseReference().toString(),
                                          SCENARIO_AAA7_CLAIM_ISSUE_CLAIM_FEE_REQUIRED.getScenario(), authToken,
                                          ScenarioRequestParams.builder().params(params).build()
                                          );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
