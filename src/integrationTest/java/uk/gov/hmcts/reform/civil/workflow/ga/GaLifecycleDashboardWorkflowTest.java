package uk.gov.hmcts.reform.civil.workflow.ga;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.workflow.ga.fixture.GaLifecycleFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_RESPONDENT;

@SuppressWarnings("java:S5960")
class GaLifecycleDashboardWorkflowTest extends GAWorkflowIntegrationTest {

    @MockBean
    private DashboardApiClient dashboardApiClient;

    @Test
    void shouldRecordResponseSubmittedScenariosForBothLipParties() throws Exception {
        GeneralApplicationCaseData caseData = GaLifecycleFixtures.paidWithResponse().copy()
            .isGaApplicantLip(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.YES)
            .build();

        startWorkflow(caseData)
            .eventId(CaseEvent.CREATE_APPLICATION_RESPONDED_DASHBOARD_NOTIFICATION)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        verifyScenario(caseData.getCcdCaseReference(),
                       SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_APPLICANT.getScenario());
        verifyScenario(caseData.getCcdCaseReference(),
                       SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_RESPONDENT.getScenario());
    }

    @Test
    void shouldReplaceNotificationsWithOfflineScenariosForAVaryJudgmentApplication() throws Exception {
        GeneralApplicationCaseData caseData = GaLifecycleFixtures.respondentVaryJudgmentWithResponse().copy()
            .isGaApplicantLip(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.YES)
            .build();

        startWorkflow(caseData)
            .eventId(CaseEvent.CREATE_APPLICATION_RESPONDED_DASHBOARD_NOTIFICATION)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        verify(dashboardApiClient).deleteNotificationsForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "APPLICANT",
            BEARER_TOKEN
        );
        verify(dashboardApiClient).deleteNotificationsForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "RESPONDENT",
            BEARER_TOKEN
        );
        verifyScenario(caseData.getCcdCaseReference(),
                       SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT.getScenario());
        verifyScenario(caseData.getCcdCaseReference(),
                       SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_RESPONDENT.getScenario());
    }

    @Test
    void shouldRecordOrderMadeScenariosForApplicantAndRespondent() throws Exception {
        GeneralApplicationCaseData caseData = GaLifecycleFixtures.decision(GAJudgeDecisionOption.FREE_FORM_ORDER);

        startWorkflow(caseData)
            .eventId(CaseEvent.CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        startWorkflow(caseData)
            .eventId(CaseEvent.CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        verifyScenario(caseData.getCcdCaseReference(),
                       SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT.getScenario());
        verifyScenario(caseData.getCcdCaseReference(),
                       SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT.getScenario());
    }

    private void verifyScenario(long caseReference, String scenario) {
        verify(dashboardApiClient).recordScenario(
            eq(Long.toString(caseReference)),
            eq(scenario),
            eq(BEARER_TOKEN),
            any()
        );
    }
}
