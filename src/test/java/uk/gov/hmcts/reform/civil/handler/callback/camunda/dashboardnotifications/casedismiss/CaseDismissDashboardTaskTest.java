package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.casedismiss;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.casedismissed.CaseDismissClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.casedismissed.CaseDismissDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseDismissDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private CaseDismissClaimantDashboardService claimantDashboardService;
    @Mock
    private CaseDismissDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        CaseDismissClaimantDashboardTask task = new CaseDismissClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyCaseDismissed(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        CaseDismissDefendantDashboardTask task = new CaseDismissDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyCaseDismissed(caseData, AUTH_TOKEN);
    }
}
