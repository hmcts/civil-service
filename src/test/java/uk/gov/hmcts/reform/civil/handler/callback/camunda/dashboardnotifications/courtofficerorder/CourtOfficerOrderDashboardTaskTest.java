package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.courtofficerorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.courtofficerorder.CourtOfficerOrderClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.courtofficerorder.CourtOfficerOrderDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtOfficerOrderDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private CourtOfficerOrderClaimantDashboardService claimantDashboardService;
    @Mock
    private CourtOfficerOrderDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = new CaseDataBuilder().caseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        CourtOfficerOrderClaimantDashboardTask task = new CourtOfficerOrderClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyCourtOfficerOrder(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        CourtOfficerOrderDefendantDashboardTask task = new CourtOfficerOrderDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyCourtOfficerOrder(caseData, AUTH_TOKEN);
    }
}
