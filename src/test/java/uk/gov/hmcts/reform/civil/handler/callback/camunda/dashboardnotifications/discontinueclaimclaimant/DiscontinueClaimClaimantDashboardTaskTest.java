package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.discontinueclaimclaimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.discontinueclaimclaimant.DiscontinueClaimClaimantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscontinueClaimClaimantDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DiscontinueClaimClaimantDashboardService dashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = new CaseDataBuilder().caseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void shouldDelegateToDashboardService() {
        DiscontinueClaimClaimantDashboardTask task = new DiscontinueClaimClaimantDashboardTask(dashboardService);

        task.execute(context);

        verify(dashboardService).notifyDiscontinueClaimClaimant(caseData, AUTH_TOKEN);
    }
}
