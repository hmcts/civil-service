package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimdismissed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimdismissed.ClaimDismissedClaimantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimDismissedClaimantDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private final CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
    @Mock
    private ClaimDismissedClaimantDashboardService claimantDashboardService;
    @Mock
    private DashboardTaskContext context;

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        ClaimDismissedClaimantDashboardTask task = new ClaimDismissedClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyClaimDismissed(caseData, AUTH_TOKEN);
    }
}
