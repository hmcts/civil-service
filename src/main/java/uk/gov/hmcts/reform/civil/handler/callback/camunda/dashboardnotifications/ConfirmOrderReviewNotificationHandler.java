package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;
import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

public abstract class ConfirmOrderReviewNotificationHandler extends DashboardCallbackHandler {

    protected final ObjectMapper objectMapper;
    protected final String role;
    protected final String taskId;
    protected final List<CaseEvent> events;
    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    @SuppressWarnings("squid:S00107")
    protected ConfirmOrderReviewNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                    DashboardNotificationsParamsMapper mapper,
                                                    FeatureToggleService featureToggleService,
                                                    ObjectMapper objectMapper,
                                                    String role,
                                                    String taskId,
                                                    List<CaseEvent> events,
                                                    DashboardNotificationService dashboardNotificationService,
                                                    TaskListService taskListService) {
        super(dashboardScenariosService, mapper, featureToggleService);
        this.objectMapper = objectMapper;
        this.role = role;
        this.taskId = taskId;
        this.events = events;
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return events;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return taskId;
    }

    @Override
    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        final String caseId = String.valueOf(caseData.getCcdCaseReference());

        if (shouldRecordScenario(caseData)) {
            taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
                caseId,
                role,
                "Applications"
            );

            dashboardNotificationService.deleteByReferenceAndCitizenRole(caseId, role);

            HashMap<String, Object> paramsMap = (HashMap<String, Object>) mapper.mapCaseDataToParams(caseData, caseEvent);

            String scenario = getScenario(caseData);
            if (!Strings.isNullOrEmpty(scenario)) {
                dashboardScenariosService.recordScenarios(
                    authToken,
                    scenario,
                    caseId,
                    ScenarioRequestParams.builder().params(paramsMap).build()
                );
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(objectMapper)).build();
    }

    @Override
    public abstract boolean shouldRecordScenario(CaseData caseData);

    @Override
    public abstract String getScenario(CaseData caseData);
}
