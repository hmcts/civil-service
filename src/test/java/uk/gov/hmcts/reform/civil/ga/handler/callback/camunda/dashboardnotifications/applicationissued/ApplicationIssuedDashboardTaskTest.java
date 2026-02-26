package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationissued;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationissued.ApplicationIssuedApplicantDashboardService;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationissued.ApplicationIssuedRespondentDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationIssuedDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private ApplicationIssuedApplicantDashboardService applicantDashboardService;
    @Mock
    private ApplicationIssuedRespondentDashboardService respondentDashboardService;
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
        ApplicationIssuedApplicantDashboardTask task =
            new ApplicationIssuedApplicantDashboardTask(applicantDashboardService);

        task.execute(context);

        verify(applicantDashboardService).notifyApplicationIssued(caseData, AUTH_TOKEN);
    }

    @Test
    void respondentTaskShouldDelegateToService() {
        ApplicationIssuedRespondentDashboardTask task =
            new ApplicationIssuedRespondentDashboardTask(respondentDashboardService);

        task.execute(context);

        verify(respondentDashboardService).notifyApplicationIssued(caseData, AUTH_TOKEN);
    }
}
