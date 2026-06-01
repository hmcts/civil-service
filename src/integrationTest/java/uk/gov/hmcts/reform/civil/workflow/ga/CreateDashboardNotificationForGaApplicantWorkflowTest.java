package uk.gov.hmcts.reform.civil.workflow.ga;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.workflow.ga.fixture.CreateDashboardNotificationForGaApplicantFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_APPLICATION_FEE_REQUIRED_APPLICANT;

@SuppressWarnings("java:S5960")
class CreateDashboardNotificationForGaApplicantWorkflowTest extends GAWorkflowIntegrationTest {

    @MockBean
    private DashboardApiClient dashboardApiClient;

    @MockBean
    private GeneralAppFeesService generalAppFeesService;

    @Test
    void shouldRecordFeeRequiredApplicantScenarioAtAboutToSubmit() throws Exception {
        var caseData = CreateDashboardNotificationForGaApplicantFixtures.caseData();
        when(generalAppFeesService.isFreeApplication(any())).thenReturn(false);

        startWorkflow(caseData)
            .eventId(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_GA_APPLICANT)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        verify(dashboardApiClient).recordScenario(
            eq(caseData.getCcdCaseReference().toString()),
            eq(SCENARIO_AAA6_GENERAL_APPS_APPLICATION_FEE_REQUIRED_APPLICANT.getScenario()),
            eq(BEARER_TOKEN),
            any()
        );
    }

    @Test
    void shouldRecordApplicationSubmittedApplicantScenarioForFreeApplication() throws Exception {
        var caseData = CreateDashboardNotificationForGaApplicantFixtures.freeApplicationCaseData();
        when(generalAppFeesService.isFreeApplication(any())).thenReturn(true);

        startWorkflow(caseData)
            .eventId(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_GA_APPLICANT)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        verify(dashboardApiClient).recordScenario(
            eq(caseData.getCcdCaseReference().toString()),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario()),
            eq(BEARER_TOKEN),
            any()
        );
    }
}
