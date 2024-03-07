package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT1_HWF_DASHBOARD_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIM_ISSUE_HWF_INVALID_REF;

@Service
@RequiredArgsConstructor
public class HwFDashboardNotificationsHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CLAIMANT1_HWF_DASHBOARD_NOTIFICATION);
    public static final String TASK_ID = "Claimant1HwFDashboardNotification";
    private final DashboardApiClient dashboardApiClient;
    private final DashboardNotificationsParamsMapper mapper;
    private final Map<CaseEvent, String> dashboardScenarios = Map.of(
        INVALID_HWF_REFERENCE,
        SCENARIO_AAA7_CLAIM_ISSUE_HWF_INVALID_REF.getScenario()
    );

    @Override

    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::configureScenarioForHwfEvents
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

    private CallbackResponse configureScenarioForHwfEvents(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        if (caseData.isHWFTypeClaimIssued() && caseData.getHwFEvent() != null) {
            dashboardApiClient.recordScenario(caseData.getCcdCaseReference().toString(),
                                              dashboardScenarios.get(caseData.getHwFEvent()), authToken,
                                              ScenarioRequestParams.builder()
                                                  .params(mapper.mapCaseDataToParams(caseData))
                                                  .build()
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();

    }
}
