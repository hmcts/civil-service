package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DashboardTaskContext context;

    private DashboardServiceTask serviceTask;

    @BeforeEach
    void setUp() {
        serviceTask = spy(new DashboardServiceTask() {
            @Override
            protected void notifyDashboard(CaseData caseData, String authToken) {
                // no-op for testing
            }
        });
    }

    @Test
    void executeShouldDelegateToNotifyDashboardWithContextValues() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);

        serviceTask.execute(context);

        verify(serviceTask).notifyDashboard(caseData, AUTH_TOKEN);
    }

    @Test
    void executeShouldRequireAuthToken() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(null);

        assertThatThrownBy(() -> serviceTask.execute(context))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Missing auth token for dashboard notification task");
    }
}
