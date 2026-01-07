package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.ClaimantResponseClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.ClaimantResponseDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private ClaimantResponseClaimantDashboardService claimantDashboardService;
    @Mock
    private ClaimantResponseDefendantDashboardService defendantDashboardService;
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
        ClaimantResponseClaimantDashboardTask task = new ClaimantResponseClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyClaimantResponse(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToService() {
        ClaimantResponseDefendantDashboardTask task = new ClaimantResponseDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyClaimantResponse(caseData, AUTH_TOKEN);
    }
}
