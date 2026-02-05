package uk.gov.hmcts.reform.civil.service.dashboardnotifications.finalorder;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardTasksHelper;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_DASHBOARD_CLAIMANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_CLAIMANT;

@Service
public class FinalOrderClaimantDashboardService extends DashboardScenarioService {

    private final DashboardNotificationHelper dashboardDecisionHelper;
    private final FeatureToggleService featureToggleService;
    private final DashboardTasksHelper dashboardTasksHelper;

    protected FinalOrderClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
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

        dashboardTasksHelper.deleteNotificationAndInactiveTasksForDefendant(caseData);

        final String scenario;

        if (dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData)) {
            scenario = SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_CLAIMANT.getScenario();
        } else {
            scenario = SCENARIO_AAA6_UPDATE_DASHBOARD_CLAIMANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS.getScenario();
        }

        return scenario;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented()
            && featureToggleService.isLipVLipEnabled()
            && dashboardDecisionHelper.isDashBoardEnabledForCase(caseData);
    }
}
