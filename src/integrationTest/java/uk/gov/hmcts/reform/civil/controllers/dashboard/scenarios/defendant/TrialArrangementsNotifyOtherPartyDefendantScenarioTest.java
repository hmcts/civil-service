package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.CaseProgressionDashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.TrialArrangementsNotifyOtherPartyDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TrialArrangementsNotifyOtherPartyDefendantScenarioTest extends CaseProgressionDashboardBaseIntegrationTest {

    @Autowired
    private TrialArrangementsNotifyOtherPartyDefendantNotificationHandler handler;

    @Test
    void should_create_notification_for_defendant_when_claimant_finalises_trial_arrangements() throws Exception {
        String caseId = "10002348";
        CaseData caseData = createCaseData(caseId, YesOrNo.NO);

        handler.handle(callbackParams(caseData));

        verifyNotificationCreated(caseId, "DEFENDANT");
        verifyTaskStatus(caseId, "CLAIMANT", TaskStatus.DONE);
    }

    @Test
    void should_update_task_list_for_claimant_when_claimant_finalises_trial_arrangements() throws Exception {
        String caseId = "10002348";
        CaseData caseData = createCaseData(caseId, YesOrNo.NO);

        handler.handle(callbackParams(caseData));

        verifyNotificationNotCreated(caseId, "DEFENDANT");
        verifyTaskStatus(caseId, "CLAIMANT", TaskStatus.DONE);
    }

    private CaseData createCaseData(String caseId, YesOrNo applicant1Represented) {
        return CaseDataBuilder.builder().atStateAwaitingResponseNotFullDefenceReceived().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(applicant1Represented)
            .respondent1Represented(YesOrNo.NO)
            .build();
    }

    private void verifyNotificationCreated(String caseId, String role) throws Exception {
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, role)
            .andExpect(status().isOk())
            .andExpectAll(
                jsonPath("$[0].titleEn").value("The other side has confirmed their trial arrangements"),
                jsonPath("$[0].titleCy").value("Mae'r parti arall wedi cadarnhau eu trefniadau treial"),
                jsonPath("$[0].descriptionEn").value("<p class=\"govuk-body\">You can <a href=\"{VIEW_ORDERS_AND_NOTICES_REDIRECT}\" class=\"govuk-link\">view the arrangements that they've confirmed</a>.</p>"),
                jsonPath("$[0].descriptionCy").value("<p class=\"govuk-body\">Gallwch <a href=\"{VIEW_ORDERS_AND_NOTICES_REDIRECT}\" class=\"govuk-link\">weld y trefniadau y maent wedi'u cadarnhau</a>.</p>")
            );
    }

    private void verifyNotificationNotCreated(String caseId, String role) throws Exception {
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, role)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    private void verifyTaskStatus(String caseId, String role, TaskStatus status) throws Exception {
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, role)
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId),
                jsonPath("$[0].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[0].currentStatusEn").value(status.getName()),
                jsonPath("$[0].taskNameCy").value("<a>Ychwanegu trefniadau'r treial</a>"),
                jsonPath("$[0].currentStatusCy").value(status.getWelshName())
            );
    }
}
