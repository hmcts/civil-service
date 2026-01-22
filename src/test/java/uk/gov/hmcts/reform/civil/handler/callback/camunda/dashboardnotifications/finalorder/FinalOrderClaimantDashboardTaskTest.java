package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.finalorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.finalorder.FinalOrderClaimantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinalOrderClaimantDashboardTaskTest {

    @Mock
    private FinalOrderClaimantDashboardService finalOrderClaimantDashboardService;

    @Mock
    private DashboardTaskContext context;

    @InjectMocks
    private FinalOrderClaimantDashboardTask finalOrderClaimantDashboardTask;

    @Test
    void shouldDelegateToFinalOrderClaimantDashboardService() {
        CaseData caseData = CaseData.builder().build();
        String authToken = "token";

        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(authToken);

        finalOrderClaimantDashboardTask.execute(context);

        verify(finalOrderClaimantDashboardService).notifyFinalOrder(caseData, authToken);
        verifyNoMoreInteractions(finalOrderClaimantDashboardService);
    }
}
