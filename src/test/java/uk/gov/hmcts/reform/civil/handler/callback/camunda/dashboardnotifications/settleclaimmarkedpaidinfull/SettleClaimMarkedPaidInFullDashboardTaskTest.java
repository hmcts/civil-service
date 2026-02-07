package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.settleclaimmarkedpaidinfull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.settleclaimmarkedpaidinfull.SettleClaimMarkedPaidInFullDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettleClaimMarkedPaidInFullDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private SettleClaimMarkedPaidInFullDashboardService dashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(123L).build();

    @BeforeEach
    void setUp() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void taskDelegatesToService() {
        SettleClaimMarkedPaidInFullDashboardTask task = new SettleClaimMarkedPaidInFullDashboardTask(dashboardService);

        task.execute(context);

        verify(dashboardService).notifySettleClaimMarkedPaidInFull(caseData, AUTH_TOKEN);
    }
}
