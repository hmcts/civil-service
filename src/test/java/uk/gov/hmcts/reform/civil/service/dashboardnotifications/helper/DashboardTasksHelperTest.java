package uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DashboardTasksHelperTest {

    private static final Long CCD_REFERENCE = 1234L;
    private static final String BASE_LOCATION = "BASE_LOCATION";

    @Mock
    private TaskListService taskListService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;

    @InjectMocks
    private DashboardTasksHelper dashboardTasksHelper;

    @Test
    void shouldExcludeApplicationsForClaimant() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);

        dashboardTasksHelper.deleteNotificationAndInactiveTasksForClaimant(caseData);

        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            CCD_REFERENCE.toString(),
            "CLAIMANT",
            "Applications"
        );
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            CCD_REFERENCE.toString(),
            "CLAIMANT");
    }

    @Test
    void shouldExcludeApplicationsForDefendant() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);

        dashboardTasksHelper.deleteNotificationAndInactiveTasksForDefendant(caseData);

        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            CCD_REFERENCE.toString(),
            "DEFENDANT",
            "Applications"
        );
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            CCD_REFERENCE.toString(),
            "DEFENDANT");
    }

    private CaseData caseData(String baseLocation, Long ccdCaseReference) {
        return new CaseDataBuilder()
            .ccdCaseReference(ccdCaseReference)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation(baseLocation))
            .build();
    }
}
