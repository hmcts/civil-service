package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.notifylipclaimanthwfoutcome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.notifylipclaimanthwfoutcome.NotifyLipClaimantHwfOutcomeDashboardService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotifyLipClaimantHwfOutcomeDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private NotifyLipClaimantHwfOutcomeDashboardService dashboardService;
    @Mock
    private CaseData caseData;

    private NotifyLipClaimantHwfOutcomeDashboardTask task;

    @BeforeEach
    void setUp() {
        task = new NotifyLipClaimantHwfOutcomeDashboardTask(dashboardService);
    }

    @Test
    void shouldDelegateToService() {
        task.notifyDashboard(caseData, AUTH_TOKEN);

        verify(dashboardService).notifyNotifyLipClaimantHwfOutcome(caseData, AUTH_TOKEN);
    }
}
