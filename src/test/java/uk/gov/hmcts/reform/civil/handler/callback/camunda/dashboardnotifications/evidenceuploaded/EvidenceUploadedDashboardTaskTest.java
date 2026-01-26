package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.evidenceuploaded;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.evidenceuploaded.EvidenceUploadedDefendantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.evidenceuploaded.EvidenceUploadedClaimantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceUploadedDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private EvidenceUploadedClaimantDashboardService claimantDashboardService;
    @Mock
    private EvidenceUploadedDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        EvidenceUploadedClaimantDashboardTask task = new EvidenceUploadedClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyEvidenceUploaded(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        EvidenceUploadedDefendantDashboardTask task = new EvidenceUploadedDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyCaseEvidenceUploaded(caseData, AUTH_TOKEN);
    }
}
