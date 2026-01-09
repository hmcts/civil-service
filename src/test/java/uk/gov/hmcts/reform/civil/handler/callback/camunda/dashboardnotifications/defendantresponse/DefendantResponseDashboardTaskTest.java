package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse.DefendantResponseClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse.DefendantResponseDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDashboardTaskTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DefendantResponseClaimantDashboardService claimantService;
    @Mock
    private DefendantResponseDefendantDashboardService defendantService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToService() {
        DefendantResponseClaimantDashboardTask task = new DefendantResponseClaimantDashboardTask(claimantService);

        task.execute(context);

        verify(claimantService).notifyDefendantResponse(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToService() {
        DefendantResponseDefendantDashboardTask task = new DefendantResponseDefendantDashboardTask(defendantService);

        task.execute(context);

        verify(defendantService).notifyDefendantResponse(caseData, AUTH_TOKEN);
    }
}
