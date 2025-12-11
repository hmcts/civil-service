package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline.CaseProceedOfflineClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline.CaseProceedOfflineDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseProceedOfflineDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private CaseProceedOfflineClaimantDashboardService claimantDashboardService;
    @Mock
    private CaseProceedOfflineDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(2L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToService() {
        CaseProceedOfflineClaimantDashboardTask task = new CaseProceedOfflineClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyCaseProceedOffline(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToService() {
        CaseProceedOfflineDefendantDashboardTask task = new CaseProceedOfflineDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyCaseProceedOffline(caseData, AUTH_TOKEN);
    }
}
