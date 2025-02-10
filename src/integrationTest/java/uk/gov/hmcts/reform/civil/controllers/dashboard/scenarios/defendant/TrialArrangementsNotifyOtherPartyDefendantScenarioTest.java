package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.CaseProgressionDashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.TrialArrangementsNotifyOtherPartyDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TrialArrangementsNotifyOtherPartyDefendantScenarioTest
    extends CaseProgressionDashboardBaseIntegrationTest {

    @Autowired
    private TrialArrangementsNotifyOtherPartyDefendantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_notification_for_defendant_when_claimant_finalises_trial_arrangements() throws Exception {

        String caseId = "10002348";
        CaseData caseData = CaseDataBuilder.builder().atStateAwaitingResponseNotFullDefenceReceived().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The other side has confirmed their trial arrangements"),
                jsonPath("$[0].titleCy").value("Mae'r parti arall wedi cadarnhau eu trefniadau treial"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">You can <a href=\"{VIEW_ORDERS_AND_NOTICES_REDIRECT}\" class=\"govuk-link\">view the arrangements that they've confirmed</a>.</p>"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Gallwch <a href=\"{VIEW_ORDERS_AND_NOTICES_REDIRECT}\" class=\"govuk-link\">weld y trefniadau y maent wedi'u cadarnhau</a>.</p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a>Add the trial arrangements</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.DONE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a>Ychwanegu trefniadau'r treial</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.DONE.getWelshName())
            );
    }
}
