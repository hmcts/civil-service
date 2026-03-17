package uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardTasksHelperTest {

    private static final Long CCD_REFERENCE = 1234L;
    private static final String BASE_LOCATION = "BASE_LOCATION";

    @Mock
    private TaskListService taskListService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;

    @InjectMocks
    private DashboardTasksHelper dashboardTasksHelper;

    @Test
    void shouldDeleteNotificationWithoutExclusionsWhenFeatureTogglesDisabled() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);
        when(featureToggleService.isLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
        when(featureToggleService.isCuiGaNroEnabled()).thenReturn(false);

        dashboardTasksHelper.deleteNotificationAndInactiveTasksForClaimant(caseData);

        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            CCD_REFERENCE.toString(),
            "CLAIMANT"
        );
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            CCD_REFERENCE.toString(),
            "CLAIMANT");
    }

    @Test
    void shouldAddApplicationsWhenLocationIsWhitelisted() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);
        when(featureToggleService.isLocationWhiteListed(BASE_LOCATION)).thenReturn(true);

        dashboardTasksHelper.deleteNotificationAndInactiveTasksForDefendant(caseData);

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            eq(CCD_REFERENCE.toString()),
            eq("DEFENDANT"),
            captor.capture()
        );
        assertThat(captor.getValue()).containsExactly("Applications");
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            CCD_REFERENCE.toString(),
            "DEFENDANT");
    }

    @Test
    void shouldAddApplicationsWhenCuiGaNroEnabled() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);
        when(featureToggleService.isLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
        when(featureToggleService.isCuiGaNroEnabled()).thenReturn(true);

        dashboardTasksHelper.deleteNotificationAndInactiveTasksForClaimant(caseData);

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            eq(CCD_REFERENCE.toString()),
            eq("CLAIMANT"),
            captor.capture()
        );
        assertThat(captor.getValue()).containsExactly("Applications");
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            CCD_REFERENCE.toString(),
            "CLAIMANT");
    }

    @Test
    void shouldIncludeProvidedCategoriesWhenTogglesDisabled() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);
        when(featureToggleService.isLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
        when(featureToggleService.isCuiGaNroEnabled()).thenReturn(false);

        dashboardTasksHelper.deleteNotificationAndInactiveTasksForClaimant(caseData, "CategoryA", "CategoryB");

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            eq(CCD_REFERENCE.toString()),
            eq("CLAIMANT"),
            captor.capture()
        );
        assertThat(captor.getValue()).containsExactly("CategoryA", "CategoryB");
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            CCD_REFERENCE.toString(),
            "CLAIMANT");
    }

    @Test
    void shouldAppendApplicationsAfterExtrasWhenToggleEnabled() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);
        when(featureToggleService.isLocationWhiteListed(BASE_LOCATION)).thenReturn(true);

        dashboardTasksHelper.deleteNotificationAndInactiveTasksForClaimant(caseData, "CategoryX");

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            eq(CCD_REFERENCE.toString()),
            eq("CLAIMANT"),
            captor.capture()
        );
        assertThat(captor.getValue()).containsExactly("CategoryX", "Applications");
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            CCD_REFERENCE.toString(),
            "CLAIMANT");
    }

    private CaseData caseData(String baseLocation, Long ccdCaseReference) {
        return new CaseDataBuilder()
            .ccdCaseReference(ccdCaseReference)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation(baseLocation))
            .build();
    }
}
