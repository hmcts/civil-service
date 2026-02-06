package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.mediationunsuccessful;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.mediationunsuccessful.MediationUnsuccessfulClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.mediationunsuccessful.MediationUnsuccessfulDefendantDashboardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MediationUnsuccessfulDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";
    @Mock
    private DashboardTaskContext context;
    @Mock
    private MediationUnsuccessfulClaimantDashboardService claimantDashboardService;
    @Mock
    private MediationUnsuccessfulDefendantDashboardService defendantDashboardService;

    private final CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        MediationUnsuccessfulClaimantDashboardTask task = new MediationUnsuccessfulClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyMediationUnsuccessful(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        MediationUnsuccessfulDefendantDashboardTask task = new MediationUnsuccessfulDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyMediationUnsuccessful(caseData, AUTH_TOKEN);
    }
}
