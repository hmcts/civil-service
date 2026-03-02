package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.finalorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.finalorder.FinalOrderApplicantDashboardService;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.finalorder.FinalOrderRespondentDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinalOrderDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private FinalOrderApplicantDashboardService applicantDashboardService;
    @Mock
    private FinalOrderRespondentDashboardService respondentDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().ccdCaseReference(3L).build();

    @BeforeEach
    void setupContext() {
        when(context.generalApplicationCaseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void applicantTaskShouldDelegateToService() {
        FinalOrderApplicantDashboardTask task =
            new FinalOrderApplicantDashboardTask(applicantDashboardService);

        task.execute(context);

        verify(applicantDashboardService).notifyFinalOrder(caseData, AUTH_TOKEN);
    }

    @Test
    void respondentTaskShouldDelegateToService() {
        FinalOrderRespondentDashboardTask task =
            new FinalOrderRespondentDashboardTask(respondentDashboardService);

        task.execute(context);

        verify(respondentDashboardService).notifyFinalOrder(caseData, AUTH_TOKEN);
    }
}
