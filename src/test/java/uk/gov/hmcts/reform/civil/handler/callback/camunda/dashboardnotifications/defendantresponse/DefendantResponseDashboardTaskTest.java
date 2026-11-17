package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse.DefendantResponseClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse.DefendantResponseDefendantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDashboardTaskTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardNotificationHelper dashboardNotificationHelper;

    @Mock
    private DefendantResponseClaimantDashboardService claimantService;
    @Mock
    private DefendantResponseDefendantDashboardService defendantService;
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
        DefendantResponseClaimantDashboardTask task = new DefendantResponseClaimantDashboardTask(claimantService, dashboardNotificationHelper);

        task.execute(context);

        verify(claimantService).notifyDefendantResponse(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToService() {
        DefendantResponseDefendantDashboardTask task = new DefendantResponseDefendantDashboardTask(defendantService, dashboardNotificationHelper);

        task.execute(context);

        verify(defendantService).notifyDefendantResponse(caseData, AUTH_TOKEN);
    }

    @Test
    void shouldDeferBothPartiesWhileWelshResponseAwaitsTranslation() {
        when(dashboardNotificationHelper.isAwaitingWelshTranslation(caseData, DEFENDANT_RESPONSE_SPEC)).thenReturn(true);

        new DefendantResponseClaimantDashboardTask(claimantService, dashboardNotificationHelper).execute(context);
        new DefendantResponseDefendantDashboardTask(defendantService, dashboardNotificationHelper).execute(context);

        verifyNoInteractions(claimantService, defendantService);
    }
}
