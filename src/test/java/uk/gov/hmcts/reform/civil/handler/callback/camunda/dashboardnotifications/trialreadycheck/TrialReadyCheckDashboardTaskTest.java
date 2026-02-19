package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadycheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadycheck.TrialReadyCheckClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadycheck.TrialReadyCheckDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrialReadyCheckDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private TrialReadyCheckClaimantDashboardService claimantDashboardService;
    @Mock
    private TrialReadyCheckDefendantDashboardService defendantDashboardService;
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
        TrialReadyCheckClaimantDashboardTask task = new TrialReadyCheckClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyTrialReadyCheck(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        TrialReadyCheckDefendantDashboardTask task = new TrialReadyCheckDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyCaseTrialReadyCheck(caseData, AUTH_TOKEN);
    }
}
