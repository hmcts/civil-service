package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_DASHBOARD_NOTIFICATIONS_RAISED_QUERY;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT;

@Service
@RequiredArgsConstructor
public class UpdateDashboardNotificationsForRaisedQuery extends CallbackHandler {

    private static final List<CaseEvent> EVENTS =
        Collections.singletonList(UPDATE_DASHBOARD_NOTIFICATIONS_RAISED_QUERY);

    public static final String TASK_ID = "UpdateDashboardNotificationsRaisedQm";
    private final DashboardScenariosService dashboardScenariosService;
    private final DashboardNotificationsParamsMapper mapper;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyPartyForQueryRaised
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

    private CallbackResponse notifyPartyForQueryRaised(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!featureToggleService.isPublicQueryManagementEnabled(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        ScenarioRequestParams
            notificationParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build();
        boolean firstQueryRaisedOnClaim = isFirstQueryRaisedOnClaim(caseData);
        if (caseData.isLipCase() && firstQueryRaisedOnClaim) {
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                notificationParams
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private boolean isFirstQueryRaisedOnClaim(CaseData caseData) {
        if (caseData.getQueries() != null) {
            return caseData.getQueries().getCaseMessages().size() == 1;
        }
        return false;
    }
}
