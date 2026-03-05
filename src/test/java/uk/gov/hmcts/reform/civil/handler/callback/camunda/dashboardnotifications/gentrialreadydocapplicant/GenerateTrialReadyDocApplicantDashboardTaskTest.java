package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.gentrialreadydocapplicant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.gentrialreadydocapplicant.GenerateTrialReadyDocApplicantDashboardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateTrialReadyDocApplicantDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private GenerateTrialReadyDocApplicantDashboardService dashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void applicantTaskShouldDelegateToDashboardService() {
        GenerateTrialReadyDocApplicantDashboardTask task = new GenerateTrialReadyDocApplicantDashboardTask(dashboardService);

        task.execute(context);

        verify(dashboardService).notifyGenerateTrialReadyDocApplicant(caseData, AUTH_TOKEN);
    }
}
