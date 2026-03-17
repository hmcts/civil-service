package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.ccjrequested;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.ccjrequested.CcjRequestedClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.ccjrequested.CcjRequestedDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcjRequestedDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private CcjRequestedClaimantDashboardService claimantDashboardService;
    @Mock
    private CcjRequestedDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(22L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToService() {
        CcjRequestedClaimantDashboardTask task = new CcjRequestedClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyClaimant(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToService() {
        CcjRequestedDefendantDashboardTask task = new CcjRequestedDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyDefendant(caseData, AUTH_TOKEN);
    }
}
