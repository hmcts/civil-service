package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc.InitiateCoscClaimantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitiateCoscClaimantDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private InitiateCoscClaimantDashboardService initiateCoscClaimantDashboardService;
    @Mock
    private DashboardTaskContext context;

    @InjectMocks
    private InitiateCoscClaimantDashboardTask task;

    @Test
    void shouldDelegateToService() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();

        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);

        task.execute(context);

        verify(initiateCoscClaimantDashboardService).notifyInitiateCosc(caseData, AUTH_TOKEN);
    }
}
