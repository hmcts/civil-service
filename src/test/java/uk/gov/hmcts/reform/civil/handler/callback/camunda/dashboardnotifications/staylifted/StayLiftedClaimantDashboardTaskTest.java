package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.staylifted;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.staylifted.StayLiftedClaimantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StayLiftedClaimantDashboardTaskTest {

    @Mock
    private StayLiftedClaimantDashboardService stayLiftedClaimantDashboardService;

    @Mock
    private DashboardTaskContext context;

    @InjectMocks
    private StayLiftedClaimantDashboardTask stayLiftedClaimantDashboardTask;

    @Test
    void shouldDelegateToStayLiftedClaimantDashboardService() {
        CaseData caseData = CaseData.builder().build();
        String authToken = "token";

        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(authToken);

        stayLiftedClaimantDashboardTask.execute(context);

        verify(stayLiftedClaimantDashboardService).notifyStayLifted(caseData, authToken);
        verifyNoMoreInteractions(stayLiftedClaimantDashboardService);
    }
}
