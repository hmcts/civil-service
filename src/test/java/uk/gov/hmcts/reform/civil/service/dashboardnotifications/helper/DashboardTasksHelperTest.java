package uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

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

    @InjectMocks
    private DashboardTasksHelper dashboardTasksHelper;

    @Test
    void shouldExcludeApplicationsForClaimantWhenLocationWhitelisted() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);
        when(featureToggleService.isLocationWhiteListed(BASE_LOCATION)).thenReturn(true);

        dashboardTasksHelper.makeTasksInactiveForClaimant(caseData);

        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            CCD_REFERENCE.toString(),
            "CLAIMANT",
            "Applications"
        );
    }

    @Test
    void shouldMakeTasksInactiveForClaimantWhenNoWhitelistOrCuiGaNro() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);
        when(featureToggleService.isLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
        when(featureToggleService.isCuiGaNroEnabled()).thenReturn(false);

        dashboardTasksHelper.makeTasksInactiveForClaimant(caseData);

        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            CCD_REFERENCE.toString(),
            "CLAIMANT"
        );
    }

    @Test
    void shouldExcludeApplicationsForClaimantWhenCuiGaNroEnabled() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);
        when(featureToggleService.isLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
        when(featureToggleService.isCuiGaNroEnabled()).thenReturn(true);

        dashboardTasksHelper.makeTasksInactiveForDefendant(caseData);

        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            CCD_REFERENCE.toString(),
            "DEFENDANT",
            "Applications"
        );
    }

    @Test
    void shouldExcludeApplicationsForDefendantWhenLocationWhitelisted() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);
        when(featureToggleService.isLocationWhiteListed(BASE_LOCATION)).thenReturn(true);

        dashboardTasksHelper.makeTasksInactiveForDefendant(caseData);

        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            CCD_REFERENCE.toString(),
            "DEFENDANT",
            "Applications"
        );
    }

    @Test
    void shouldMakeTasksInactiveForDefendantWhenNoWhitelistOrCuiGaNro() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);
        when(featureToggleService.isLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
        when(featureToggleService.isCuiGaNroEnabled()).thenReturn(false);

        dashboardTasksHelper.makeTasksInactiveForDefendant(caseData);

        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            CCD_REFERENCE.toString(),
            "DEFENDANT"
        );
    }

    @Test
    void shouldExcludeApplicationsForDefendantWhenCuiGaNroEnabled() {
        CaseData caseData = caseData(BASE_LOCATION, CCD_REFERENCE);
        when(featureToggleService.isLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
        when(featureToggleService.isCuiGaNroEnabled()).thenReturn(true);

        dashboardTasksHelper.makeTasksInactiveForDefendant(caseData);

        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            CCD_REFERENCE.toString(),
            "DEFENDANT",
            "Applications"
        );
    }

    private CaseData caseData(String baseLocation, Long ccdCaseReference) {
        return new CaseDataBuilder()
            .ccdCaseReference(ccdCaseReference)
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation(baseLocation)
                                        .build())
            .build();
    }
}
