package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadyrespondent1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadyrespondent1.TrialReadyCheckRespondent1ClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadyrespondent1.TrialReadyCheckRespondent1DefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrialReadyCheckRespondent1DashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private TrialReadyCheckRespondent1ClaimantDashboardService claimantDashboardService;
    @Mock
    private TrialReadyCheckRespondent1DefendantDashboardService defendantDashboardService;
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
        TrialReadyCheckRespondent1ClaimantDashboardTask task = new TrialReadyCheckRespondent1ClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyTrialReadyCheckRespondent1(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        TrialReadyCheckRespondent1DefendantDashboardTask task = new TrialReadyCheckRespondent1DefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyCaseTrialReadyCheckRespondent1(caseData, AUTH_TOKEN);
    }
}
