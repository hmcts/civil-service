package uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

@Service
public class DashboardTasksHelper {

    private final TaskListService taskListService;
    private final FeatureToggleService featureToggleService;
    private final DashboardNotificationService dashboardNotificationService;

    public DashboardTasksHelper(TaskListService taskListService,
                                FeatureToggleService featureToggleService,
                                DashboardNotificationService dashboardNotificationService) {
        this.taskListService = taskListService;
        this.featureToggleService = featureToggleService;
        this.dashboardNotificationService = dashboardNotificationService;
    }

    public void deleteNotificationAndInactiveTasksForClaimant(CaseData caseData) {
        deleteNotificationAndInactiveTasksForRole(caseData, "CLAIMANT");
    }

    public void deleteNotificationAndInactiveTasksForDefendant(CaseData caseData) {
        deleteNotificationAndInactiveTasksForRole(caseData, "DEFENDANT");
    }

    private void deleteNotificationAndInactiveTasksForRole(CaseData caseData, String citizenRole) {
        dashboardNotificationService.deleteByReferenceAndCitizenRole(
            caseData.getCcdCaseReference().toString(),
            citizenRole
        );

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
