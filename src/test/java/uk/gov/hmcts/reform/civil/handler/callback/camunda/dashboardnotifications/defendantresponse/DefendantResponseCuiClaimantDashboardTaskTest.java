package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse.DefendantResponseCuiClaimantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantResponseCuiClaimantDashboardTaskTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DefendantResponseCuiClaimantDashboardService claimantService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = new CaseDataBuilder().caseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToService() {
        DefendantResponseCuiClaimantDashboardTask task = new DefendantResponseCuiClaimantDashboardTask(claimantService);

        task.execute(context);

        verify(claimantService).notifyDefendantResponse(caseData, AUTH_TOKEN);
    }
}
