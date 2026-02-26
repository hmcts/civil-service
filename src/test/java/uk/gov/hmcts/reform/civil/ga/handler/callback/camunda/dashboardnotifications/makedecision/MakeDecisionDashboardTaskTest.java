package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.makedecision;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.MakeDecisionApplicantDashboardService;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.MakeDecisionRespondentDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MakeDecisionDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private MakeDecisionApplicantDashboardService applicantDashboardService;
    @Mock
    private MakeDecisionRespondentDashboardService respondentDashboardService;
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
        MakeDecisionApplicantDashboardTask task =
            new MakeDecisionApplicantDashboardTask(applicantDashboardService);

        task.execute(context);

        verify(applicantDashboardService).notifyMakeDecision(caseData, AUTH_TOKEN);
    }

    @Test
    void respondentTaskShouldDelegateToService() {
        MakeDecisionRespondentDashboardTask task =
            new MakeDecisionRespondentDashboardTask(respondentDashboardService);

        task.execute(context);

        verify(respondentDashboardService).notifyMakeDecision(caseData, AUTH_TOKEN);
    }
}
