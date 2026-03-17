package uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DashboardTasksHelper {

    private final TaskListService taskListService;
    private final FeatureToggleService featureToggleService;
    private final DashboardNotificationService dashboardNotificationService;

    private static final String APPLICATIONS = "Applications";

    public DashboardTasksHelper(TaskListService taskListService,
                                FeatureToggleService featureToggleService,
                                DashboardNotificationService dashboardNotificationService) {
        this.taskListService = taskListService;
        this.featureToggleService = featureToggleService;
        this.dashboardNotificationService = dashboardNotificationService;
    }

    public void deleteNotificationAndInactiveTasksForClaimant(CaseData caseData, String... excludeCategory) {
        deleteNotificationAndInactiveTasksForRole(caseData, "CLAIMANT", excludeCategory);
    }

    public void deleteNotificationAndInactiveTasksForDefendant(CaseData caseData, String... excludeCategories) {
        deleteNotificationAndInactiveTasksForRole(caseData, "DEFENDANT", excludeCategories);
    }

    private void deleteNotificationAndInactiveTasksForRole(
        CaseData caseData,
        String citizenRole,
        String... excludeCategories) {

        String caseReference = caseData.getCcdCaseReference().toString();

        dashboardNotificationService.deleteByReferenceAndCitizenRole(caseReference, citizenRole);

        List<String> categories = buildCategories(caseData, excludeCategories);

        if (categories.isEmpty()) {
            taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(caseReference, citizenRole);
        } else {
            taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
                caseReference,
                citizenRole,
                categories.toArray(new String[0])
            );
        }
    }

    private List<String> buildCategories(CaseData caseData, String... excludeCategories) {
        List<String> categories = new ArrayList<>();

        if (excludeCategories != null) {
            categories.addAll(Arrays.asList(excludeCategories));
        }

        if (featureToggleService.isLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation())
            || featureToggleService.isCuiGaNroEnabled()) {
            categories.add(APPLICATIONS);
        }

        return categories;
    }
}
