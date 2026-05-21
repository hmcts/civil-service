package uk.gov.hmcts.reform.civil.workflow.ga;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.workflow.ga.fixture.CreateApplicationSubmittedDashboardNotificationForRespondentFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT;

@SuppressWarnings("java:S5960")
class CreateApplicationSubmittedDashboardNotificationForRespondentWorkflowTest
    extends GAWorkflowIntegrationTest {

    @MockBean
    private DashboardApiClient dashboardApiClient;

    @Test
    void shouldRecordNonUrgentRespondentScenarioAtAboutToSubmit() throws Exception {
        var caseData = CreateApplicationSubmittedDashboardNotificationForRespondentFixtures.caseData();

        startWorkflow(caseData)
            .eventId(CaseEvent.CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        verify(dashboardApiClient).recordScenario(
            eq(caseData.getCcdCaseReference().toString()),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT.getScenario()),
            eq(BEARER_TOKEN),
            any()
        );
    }

    @Test
    void shouldRecordUrgentRespondentScenarioAtAboutToSubmit() throws Exception {
        var caseData = CreateApplicationSubmittedDashboardNotificationForRespondentFixtures.urgentCaseData();

        startWorkflow(caseData)
            .eventId(CaseEvent.CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        verify(dashboardApiClient).recordScenario(
            eq(caseData.getCcdCaseReference().toString()),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT.getScenario()),
            eq(BEARER_TOKEN),
            any()
        );
    }
}
