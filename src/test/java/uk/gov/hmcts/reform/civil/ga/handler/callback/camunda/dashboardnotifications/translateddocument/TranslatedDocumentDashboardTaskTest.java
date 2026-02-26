package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.translateddocument;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.translateddocument.TranslatedDocumentApplicantDashboardService;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.translateddocument.TranslatedDocumentRespondentDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslatedDocumentDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private TranslatedDocumentApplicantDashboardService applicantDashboardService;
    @Mock
    private TranslatedDocumentRespondentDashboardService respondentDashboardService;
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
        TranslatedDocumentApplicantDashboardTask task =
            new TranslatedDocumentApplicantDashboardTask(applicantDashboardService);

        task.execute(context);

        verify(applicantDashboardService).notifyTranslatedDocument(caseData, AUTH_TOKEN);
    }

    @Test
    void respondentTaskShouldDelegateToService() {
        TranslatedDocumentRespondentDashboardTask task =
            new TranslatedDocumentRespondentDashboardTask(respondentDashboardService);

        task.execute(context);

        verify(respondentDashboardService).notifyTranslatedDocument(caseData, AUTH_TOKEN);
    }
}
