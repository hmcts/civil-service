package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantnoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantnoc.ClaimantNocOnlineDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantnoc.DefendantNocClaimantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantNocDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DefendantNocClaimantDashboardService offlineDashboardService;
    @Mock
    private ClaimantNocOnlineDashboardService onlineDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(22L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void offlineTaskShouldDelegateToService() {
        DefendantNocClaimantDashboardTask task = new DefendantNocClaimantDashboardTask(offlineDashboardService);

        task.execute(context);

        verify(offlineDashboardService).notifyClaimant(caseData, AUTH_TOKEN);
    }

    @Test
    void onlineTaskShouldDelegateToService() {
        ClaimantNocOnlineDashboardTask task = new ClaimantNocOnlineDashboardTask(onlineDashboardService);

        task.execute(context);

        verify(onlineDashboardService).notifyClaimant(caseData, AUTH_TOKEN);
    }
}
