package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.createlipclaim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.createlipclaim.CreateLipClaimDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateLipClaimDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private CreateLipClaimDashboardService dashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(123L).build();

    @BeforeEach
    void setUp() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void taskShouldDelegateToDashboardService() {
        CreateLipClaimDashboardTask task = new CreateLipClaimDashboardTask(dashboardService);

        task.execute(context);

        verify(dashboardService).notifyCreateLipClaim(caseData, AUTH_TOKEN);
    }
}
