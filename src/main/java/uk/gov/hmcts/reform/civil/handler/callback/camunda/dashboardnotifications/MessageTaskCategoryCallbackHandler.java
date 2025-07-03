package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_MESSAGES_TASK_CATEGORIES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MESSAGES_TO_THE_COURT;

@Service
public class MessageTaskCategoryCallbackHandler extends DashboardCallbackHandler {

    public static final String TASK_ID = "CreateIssueClaimDashboardNotificationsForApplicant1";
    private static final List<CaseEvent> EVENTS = List.of(CREATE_MESSAGES_TASK_CATEGORIES);

    public MessageTaskCategoryCallbackHandler(DashboardScenariosService dashboardScenariosService,
                                                DashboardNotificationsParamsMapper mapper,
                                                FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return null;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        dashboardScenariosService.recordScenarios(
            authToken,
            SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder()
                .params(mapper.mapCaseDataToParams(caseData)).build()
        );
        dashboardScenariosService.recordScenarios(
            authToken,
            SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder()
                .params(mapper.mapCaseDataToParams(caseData)).build()
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }
}
