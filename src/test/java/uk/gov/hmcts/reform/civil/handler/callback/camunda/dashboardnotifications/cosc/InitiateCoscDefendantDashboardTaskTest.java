package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc.InitiateCoscDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitiateCoscDefendantDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private InitiateCoscDefendantDashboardService initiateCoscDefendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    @InjectMocks
    private InitiateCoscDefendantDashboardTask task;

    @Test
    void shouldDelegateToService() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().ccdCaseReference(1L);

        when(context.generalApplicationCaseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);

        task.execute(context);

        verify(initiateCoscDefendantDashboardService).notifyInitiateCosc(caseData, AUTH_TOKEN);
    }
}
