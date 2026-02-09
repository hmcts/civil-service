package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.mediationsuccessful;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.mediationsuccessful.MediationSuccessfulClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.mediationsuccessful.MediationSuccessfulDefendantDashboardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MediationSuccessfulDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";
    @Mock
    private DashboardTaskContext context;
    @Mock
    private MediationSuccessfulClaimantDashboardService claimantDashboardService;
    @Mock
    private MediationSuccessfulDefendantDashboardService defendantDashboardService;

    private final CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        MediationSuccessfulClaimantDashboardTask task = new MediationSuccessfulClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyMediationSuccessful(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        MediationSuccessfulDefendantDashboardTask task = new MediationSuccessfulDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyMediationSuccessful(caseData, AUTH_TOKEN);
    }
}
