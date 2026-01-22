package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

@Service
public class ClaimIssueDefendantDashboardService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;
    private final FeatureToggleService featureToggleService;

    public ClaimIssueDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                DashboardNotificationsParamsMapper mapper,
                                                FeatureToggleService featureToggleService,
                                                DashboardNotificationService dashboardNotificationService,
                                                TaskListService taskListService) {
        super(dashboardScenariosService, mapper);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
        this.featureToggleService = featureToggleService;
    }

    public void notifyClaimIssue(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }


    @Override
    protected String getScenario(CaseData caseData) {
        return "";
    }
}
