package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationsubmitted;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationsubmitted.ApplicationSubmittedApplicantDashboardService;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationsubmitted.ApplicationSubmittedRespondentDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmittedDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private ApplicationSubmittedApplicantDashboardService applicantDashboardService;
    @Mock
    private ApplicationSubmittedRespondentDashboardService respondentDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder().ccdCaseReference(3L).build();

    @BeforeEach
    void setupContext() {
        when(context.generalApplicationCaseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void applicantTaskShouldDelegateToService() {
        ApplicationSubmittedApplicantDashboardTask task =
            new ApplicationSubmittedApplicantDashboardTask(applicantDashboardService);

        task.execute(context);

        verify(applicantDashboardService).notifyApplicationSubmitted(caseData, AUTH_TOKEN);
    }

    @Test
    void respondentTaskShouldDelegateToService() {
        ApplicationSubmittedRespondentDashboardTask task =
            new ApplicationSubmittedRespondentDashboardTask(respondentDashboardService);

        task.execute(context);

        verify(respondentDashboardService).notifyApplicationSubmitted(caseData, AUTH_TOKEN);
    }
}
