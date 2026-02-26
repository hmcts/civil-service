package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationresponded;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationresponded.ApplicationRespondedDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationRespondedDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private ApplicationRespondedDashboardService dashboardService;
    @Mock
    private DashboardTaskContext context;

    private final GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().ccdCaseReference(3L).build();

    @BeforeEach
    void setupContext() {
        when(context.generalApplicationCaseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void taskShouldDelegateToService() {
        ApplicationRespondedDashboardTask task = new ApplicationRespondedDashboardTask(dashboardService);

        task.execute(context);

        verify(dashboardService).notifyApplicationResponded(caseData, AUTH_TOKEN);
    }
}
