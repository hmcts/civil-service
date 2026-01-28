package uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

@Service
public class DashboardTasksHelper {

    private final TaskListService taskListService;
    private final FeatureToggleService featureToggleService;

    public DashboardTasksHelper(TaskListService taskListService, FeatureToggleService featureToggleService) {
        this.taskListService = taskListService;
        this.featureToggleService = featureToggleService;
    }

    public void makeTasksInactiveForClaimant(CaseData caseData) {
        makeTasksInactiveForRole(caseData, "CLAIMANT");
    }

    public void makeTasksInactiveForDefendant(CaseData caseData) {
        makeTasksInactiveForRole(caseData, "DEFENDANT");
    }

    private void makeTasksInactiveForRole(CaseData caseData, String citizenRole) {
        if (featureToggleService.isLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation())
            || featureToggleService.isCuiGaNroEnabled()) {
            taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
                caseData.getCcdCaseReference().toString(),
                citizenRole,
                "Applications"
            );
        } else {
            taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
                caseData.getCcdCaseReference().toString(),
                citizenRole
            );
        }
    }
}
