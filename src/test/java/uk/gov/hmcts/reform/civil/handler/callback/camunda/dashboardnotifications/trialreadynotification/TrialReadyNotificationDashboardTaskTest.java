package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadynotification;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadynotification.TrialReadyNotificationClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadynotification.TrialReadyNotificationDefendantDashboardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrialReadyNotificationDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private TrialReadyNotificationClaimantDashboardService claimantDashboardService;
    @Mock
    private TrialReadyNotificationDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        TrialReadyNotificationClaimantDashboardTask task = new TrialReadyNotificationClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyTrialReadyNotification(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        TrialReadyNotificationDefendantDashboardTask task = new TrialReadyNotificationDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyTrialReadyNotification(caseData, AUTH_TOKEN);
    }
}
