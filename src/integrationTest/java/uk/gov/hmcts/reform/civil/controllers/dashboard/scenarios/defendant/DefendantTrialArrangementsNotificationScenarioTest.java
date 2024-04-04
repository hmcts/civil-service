package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_REQUIRED_DEFENDANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class DefendantTrialArrangementsNotificationScenarioTest extends BaseIntegrationTest {

    @Test
    void shouldCreateAddTrialArrangementsForDefendant() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("respondent1PartyName", "Mr Defendant");
        UUID caseId = UUID.randomUUID();
        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder()
                .params(map).build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_REQUIRED_DEFENDANT.getScenario(),
            caseId
        ).andExpect(status().isOk());

        //Verify Notification is created for DEFENDANT
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "Confirm your trial arrangements"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You must <a href=\"{TRIAL_ARRANGEMENTS}\" class=\"govuk-link\">confirm your trial arrangements</a> by ${respondent1ResponseDeadlineEn}. This means that you’ll need to confirm if the case is ready for trial or not. You’ll also need to confirm whether circumstances have changed since you completed the directions questionnaire. Refer to the <a href=\"{QUESTIONNAIRE_SUBMITTED}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">questionnaire you submitted</a> if you’re not sure what you previously said.</p>"),
            jsonPath("$[0].titleCy").value(
                "Confirm your trial arrangements"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">You must <a href=\"{TRIAL_ARRANGEMENTS}\" class=\"govuk-link\">confirm your trial arrangements</a> by ${respondent1ResponseDeadlineCy}. This means that you’ll need to confirm if the case is ready for trial or not. You’ll also need to confirm whether circumstances have changed since you completed the directions questionnaire. Refer to the <a href=\"{QUESTIONNAIRE_SUBMITTED}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">questionnaire you submitted</a> if you’re not sure what you previously said.</p>")
        );
    }
}
