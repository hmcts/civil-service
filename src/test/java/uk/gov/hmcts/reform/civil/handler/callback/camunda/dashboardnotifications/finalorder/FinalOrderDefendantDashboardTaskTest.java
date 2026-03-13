package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.finalorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.finalorder.FinalOrderDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinalOrderDefendantDashboardTaskTest {

    @Mock
    private FinalOrderDefendantDashboardService finalOrderDefendantDashboardService;

    @Mock
    private DashboardTaskContext context;

    @InjectMocks
    private FinalOrderDefendantDashboardTask finalOrderDefendantDashboardTask;

    @Test
    void shouldDelegateToFinalOrderDefendantDashboardService() {
        CaseData caseData = CaseData.builder().build();
        String authToken = "token";

        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(authToken);

        finalOrderDefendantDashboardTask.execute(context);

        verify(finalOrderDefendantDashboardService).notifyFinalOrder(caseData, authToken);
        verifyNoMoreInteractions(finalOrderDefendantDashboardService);
    }
}
