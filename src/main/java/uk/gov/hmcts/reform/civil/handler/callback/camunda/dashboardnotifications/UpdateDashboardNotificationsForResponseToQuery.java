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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_DASHBOARD_NOTIFICATIONS_RESPONSE_TO_QUERY;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT_DELETE;

@Service
@RequiredArgsConstructor
public class UpdateDashboardNotificationsForResponseToQuery extends CallbackHandler {

    private static final List<CaseEvent> EVENTS =
        Collections.singletonList(UPDATE_DASHBOARD_NOTIFICATIONS_RESPONSE_TO_QUERY);

    public static final String TASK_ID = "UpdateDashboardNotificationsResponseToQuery";
    private final DashboardScenariosService dashboardScenariosService;
    private final DashboardNotificationsParamsMapper mapper;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::createDashboardNotification
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

    private CallbackResponse createDashboardNotification(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!featureToggleService.isPublicQueryManagementEnabled(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        ScenarioRequestParams
            notificationParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        if (caseData.isApplicantLiP()) {
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT_DELETE.getScenario(),
                caseData.getCcdCaseReference().toString(),
                notificationParams
            );
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                notificationParams
            );
        }
        if (caseData.isRespondent1LiP()) {
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT_DELETE.getScenario(),
                caseData.getCcdCaseReference().toString(),
                notificationParams
            );
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                notificationParams
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
