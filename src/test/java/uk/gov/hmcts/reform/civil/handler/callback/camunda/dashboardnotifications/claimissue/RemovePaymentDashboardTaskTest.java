package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimissue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;

@ExtendWith(MockitoExtension.class)
class RemovePaymentDashboardTaskTest {

    @Mock
    private DashboardNotificationService service;
    @Mock
    private DashboardNotificationHelper dashboardNotificationHelper;

    @Test
    void shouldDeleteOnlyTheRetiredNotificationForClaimant() {
        CaseData caseData = caseData(LocalDateTime.now().minusDays(1));
        when(dashboardNotificationHelper.isAwaitingWelshTranslation(caseData, CREATE_CLAIM_SPEC_AFTER_PAYMENT)).thenReturn(true);

        new RemovePaymentDashboardTask(dashboardNotificationHelper, service).execute(DashboardTaskContext.civil(caseData, "token"));

        verify(service).deleteByNameAndReferenceAndCitizenRole("Notice.AAA6.ClaimIssue.ClaimFee.Required", "123", "CLAIMANT");
    }

    @Test
    void shouldSkipRemovalOutsideTheWelshSubmissionBranch() {
        CaseData caseData = caseData(LocalDateTime.now().minusDays(1));

        new RemovePaymentDashboardTask(dashboardNotificationHelper, service).execute(DashboardTaskContext.civil(caseData, "token"));

        verifyNoInteractions(service);
    }

    private CaseData caseData(LocalDateTime deadline) {
        return CaseData.builder().ccdCaseReference(123L).respondent1ResponseDeadline(deadline).build();
    }
}
