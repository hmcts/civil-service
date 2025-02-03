package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseEventsDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_DASHBOARD_DEFENDANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_DEFENDANT;

@Service
public class ConfirmOrderReviewDefendantNotificationHandler extends CaseEventsDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT);
    public static final String TASK_ID = "UpdateTaskListConfirmOrderReviewDefendant";
    public static final String GA = "Applications";
    private final ObjectMapper objectMapper;

    public ConfirmOrderReviewDefendantNotificationHandler(DashboardApiClient dashboardApiClient,
                                                          DashboardNotificationsParamsMapper mapper,
                                                          FeatureToggleService featureToggleService,
                                                          ObjectMapper objectMapper) {
        super(dashboardApiClient, mapper, featureToggleService);
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (isOrderMadeFastTrackTrialNotResponded(caseData)) {
            return SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_DEFENDANT.getScenario();
        }
        return SCENARIO_AAA6_UPDATE_DASHBOARD_DEFENDANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS.getScenario();
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented()
            && YesOrNo.YES.equals(caseData.getIsFinalOrder());
    }

    @Override
    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());

        if (shouldRecordScenario(caseData)) {
            dashboardApiClient.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
                caseData.getCcdCaseReference().toString(),
                "DEFENDANT",
                GA,
                authToken
            );
            dashboardApiClient.deleteNotificationsForCaseIdentifierAndRole(
                caseData.getCcdCaseReference().toString(),
                "DEFENDANT",
                authToken
            );

            HashMap<String, Object> paramsMap = (HashMap<String, Object>) mapper.mapCaseDataToParams(caseData, caseEvent);

            String scenario = getScenario(caseData);
            if (!Strings.isNullOrEmpty(scenario)) {
                dashboardApiClient.recordScenario(
                    caseData.getCcdCaseReference().toString(),
                    scenario,
                    authToken,
                    ScenarioRequestParams.builder().params(paramsMap).build()
                );
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(objectMapper)).build();
    }

    private boolean isOrderMadeFastTrackTrialNotResponded(CaseData caseData) {
        return SdoHelper.isFastTrack(caseData) && isNull(caseData.getTrialReadyRespondent1());
    }
}
