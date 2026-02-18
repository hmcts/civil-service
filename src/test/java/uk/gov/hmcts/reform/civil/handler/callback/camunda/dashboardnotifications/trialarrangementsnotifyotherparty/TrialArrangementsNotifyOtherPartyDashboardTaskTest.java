package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialarrangementsnotifyotherparty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialarrangementsnotifyotherparty.TrialArrangementsNotifyOtherPartyClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialarrangementsnotifyotherparty.TrialArrangementsNotifyOtherPartyDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrialArrangementsNotifyOtherPartyDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private TrialArrangementsNotifyOtherPartyClaimantDashboardService claimantDashboardService;
    @Mock
    private TrialArrangementsNotifyOtherPartyDefendantDashboardService defendantDashboardService;
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
        TrialArrangementsNotifyOtherPartyClaimantDashboardTask task = new TrialArrangementsNotifyOtherPartyClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyTrialArrangementsNotifyOtherParty(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        TrialArrangementsNotifyOtherPartyDefendantDashboardTask task = new TrialArrangementsNotifyOtherPartyDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyTrialArrangementsNotifyOtherParty(caseData, AUTH_TOKEN);
    }
}
