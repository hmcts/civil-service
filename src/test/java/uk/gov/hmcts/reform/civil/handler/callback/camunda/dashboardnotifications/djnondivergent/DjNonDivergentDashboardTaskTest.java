package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.djnondivergent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.djnondivergent.DjNonDivergentClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.djnondivergent.DjNonDivergentDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjNonDivergentDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DjNonDivergentClaimantDashboardService claimantDashboardService;
    @Mock
    private DjNonDivergentDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        DjNonDivergentClaimantDashboardTask task = new DjNonDivergentClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyDjNonDivergent(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        DjNonDivergentDefendantDashboardTask task = new DjNonDivergentDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyDjNonDivergent(caseData, AUTH_TOKEN);
    }
}
