package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_NOC_ONLINE_DASHBOARD_NOTIFICATION_FOR_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_CLAIM_REMAINS_ONLINE_CLAIMANT;

@Service
@RequiredArgsConstructor
public class ClaimantNocOnlineDashboardNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_NOC_ONLINE_DASHBOARD_NOTIFICATION_FOR_CLAIMANT);
    public static final String TASK_ID = "createOnlineDashboardNotificationForClaimant";
    private static final String CLAIMANT_ROLE = "CLAIMANT";

    private final DashboardScenariosService dashboardScenariosService;
    private final DashboardNotificationsParamsMapper mapper;
    private final FeatureToggleService toggleService;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::configureScenarioForClaimantNotification
        );
    }

    private CallbackResponse configureScenarioForClaimantNotification(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!toggleService.isDefendantNoCOnlineForCase(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        ScenarioRequestParams params =
            ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build();

        dashboardScenariosService.recordScenarios(
            authToken,
            SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_CLAIM_REMAINS_ONLINE_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            params
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
