package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimissue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue.CreateClaimAfterPaymentApplicantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue.CreateClaimAfterPaymentDefendantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentDashboardTaskTest {

    @Mock
    private CreateClaimAfterPaymentApplicantDashboardService applicantService;
    @Mock
    private CreateClaimAfterPaymentDefendantDashboardService defendantService;
    @Mock
    private DashboardNotificationHelper helper;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(123L).build();

    @Test
    void shouldCreateNotificationsForBothParties() {
        DashboardTaskContext context = DashboardTaskContext.civil(caseData, "token");

        new CreateClaimAfterPaymentApplicantDashboardTask(applicantService, helper).execute(context);
        new CreateClaimAfterPaymentDefendantDashboardTask(defendantService, helper).execute(context);

        verify(applicantService).notifyClaimant(caseData, "token");
        verify(defendantService).notifyDefendant(caseData, "token");
    }

    @Test
    void shouldDeferBothPartiesWhileWelshClaimAwaitsTranslation() {
        when(helper.isAwaitingWelshTranslation(caseData, CREATE_CLAIM_SPEC_AFTER_PAYMENT)).thenReturn(true);
        DashboardTaskContext context = DashboardTaskContext.civil(caseData, "token");

        new CreateClaimAfterPaymentApplicantDashboardTask(applicantService, helper).execute(context);
        new CreateClaimAfterPaymentDefendantDashboardTask(defendantService, helper).execute(context);

        verifyNoInteractions(applicantService, defendantService);
    }
}
