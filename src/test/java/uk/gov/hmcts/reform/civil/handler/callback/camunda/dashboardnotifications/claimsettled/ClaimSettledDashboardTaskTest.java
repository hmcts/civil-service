package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimsettled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimsettled.ClaimSettledClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimsettled.ClaimSettledDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimSettledDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private ClaimSettledClaimantDashboardService claimantService;
    @Mock
    private ClaimSettledDefendantDashboardService defendantService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setup() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskDelegatesToService() {
        ClaimSettledClaimantDashboardTask task = new ClaimSettledClaimantDashboardTask(claimantService);

        task.execute(context);

        verify(claimantService).notifyClaimSettled(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskDelegatesToService() {
        ClaimSettledDefendantDashboardTask task = new ClaimSettledDefendantDashboardTask(defendantService);

        task.execute(context);

        verify(defendantService).notifyClaimSettled(caseData, AUTH_TOKEN);
    }
}
