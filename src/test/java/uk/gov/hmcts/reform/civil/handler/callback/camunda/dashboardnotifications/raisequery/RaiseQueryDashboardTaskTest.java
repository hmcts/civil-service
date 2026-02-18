package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.raisequery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.raisequery.RaiseQueryDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaiseQueryDashboardTaskTest {

    private static final String AUTH_TOKEN = "auth";

    @Mock
    private RaiseQueryDashboardService dashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseDataBuilder.builder().build();

    @BeforeEach
    void setUpContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void shouldDelegateToDashboardService() {
        RaiseQueryDashboardTask task = new RaiseQueryDashboardTask(dashboardService);

        task.execute(context);

        verify(dashboardService).notifyRaiseQuery(caseData, AUTH_TOKEN);
    }
}
