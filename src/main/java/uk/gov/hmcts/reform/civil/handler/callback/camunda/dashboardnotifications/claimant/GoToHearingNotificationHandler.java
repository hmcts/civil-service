package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_GO_TO_HEARING_FOR_APPLICANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_GO_TO_HEARING;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

@Service
@RequiredArgsConstructor
public class GoToHearingNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_GO_TO_HEARING_FOR_APPLICANT1);
    public static final String TASK_ID = "GenerateDashboardNotificationGoToHearing";
    private final DashboardApiClient dashboardApiClient;
    private final DashboardNotificationsParamsMapper mapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::configureScenarioForGoToHearing
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

    private CallbackResponse configureScenarioForGoToHearing(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getCcdState() == CaseState.JUDICIAL_REFERRAL) {
            String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

            dashboardApiClient.recordScenario(caseData.getCcdCaseReference().toString(),
                                              SCENARIO_AAA7_CLAIMANT_INTENT_GO_TO_HEARING.getScenario(), authToken,
                                              ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build()
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
