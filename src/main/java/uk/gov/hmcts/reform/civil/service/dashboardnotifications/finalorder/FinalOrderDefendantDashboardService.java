package uk.gov.hmcts.reform.civil.service.dashboardnotifications.finalorder;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardTasksHelper;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_DASHBOARD_DEFENDANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_DEFENDANT;

@Service
public class FinalOrderDefendantDashboardService extends DashboardScenarioService {

    private final DashboardNotificationHelper dashboardDecisionHelper;
    private final FeatureToggleService featureToggleService;
    private final DashboardTasksHelper dashboardTasksHelper;

    protected FinalOrderDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                  DashboardNotificationsParamsMapper mapper,
                                                  DashboardNotificationHelper dashboardDecisionHelper,
                                                  FeatureToggleService featureToggleService,
                                                  DashboardTasksHelper dashboardTasksHelper) {
        super(dashboardScenariosService, mapper);
        this.dashboardDecisionHelper = dashboardDecisionHelper;
        this.featureToggleService = featureToggleService;
        this.dashboardTasksHelper = dashboardTasksHelper;
    }

    public void notifyFinalOrder(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {

        dashboardTasksHelper.makeTasksInactiveForDefendant(caseData);

        final String scenario;

        if (dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData)) {
            scenario = SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_DEFENDANT.getScenario();
        } else {
            scenario = SCENARIO_AAA6_UPDATE_DASHBOARD_DEFENDANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS.getScenario();
        }

        return scenario;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented()
            && featureToggleService.isLipVLipEnabled()
            && dashboardDecisionHelper.isDashBoardEnabledForCase(caseData);
    }
}
