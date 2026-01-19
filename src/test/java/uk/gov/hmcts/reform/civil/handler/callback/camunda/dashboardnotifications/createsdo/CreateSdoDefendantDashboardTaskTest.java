package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.createsdo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.createsdo.CreateSdoDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSdoDefendantDashboardTaskTest {

    @Mock
    private CreateSdoDefendantDashboardService createSdoDefendantDashboardService;

    @Mock
    private DashboardTaskContext context;

    @InjectMocks
    private CreateSdoDefendantDashboardTask createSdoDefendantDashboardTask;

    @Test
    void shouldDelegateToCreateSdoDefendantDashboardService() {
        CaseData caseData = CaseData.builder().build();
        String authToken = "token";

        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(authToken);

        createSdoDefendantDashboardTask.execute(context);

        verify(createSdoDefendantDashboardService).notifyBundleUpdated(caseData, authToken);
        verifyNoMoreInteractions(createSdoDefendantDashboardService);
    }
}
