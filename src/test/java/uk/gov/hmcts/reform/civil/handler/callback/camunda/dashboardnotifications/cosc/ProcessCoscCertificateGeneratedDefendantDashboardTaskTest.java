package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc.CertificateGeneratedDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessCoscCertificateGeneratedDefendantDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private CertificateGeneratedDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(3L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToService() {
        ProcessCoscCertificateGeneratedDefendantDashboardTask task =
            new ProcessCoscCertificateGeneratedDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyCertificateGenerated(caseData, AUTH_TOKEN);
    }
}
