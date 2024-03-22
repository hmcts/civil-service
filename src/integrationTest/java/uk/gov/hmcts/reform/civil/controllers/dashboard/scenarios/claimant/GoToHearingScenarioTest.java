package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_GO_TO_HEARING;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class GoToHearingScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_go_to_hearing_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(Map.of("respondent1PartyName", "Mr Def Defendant"))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA7_CLAIMANT_INTENT_GO_TO_HEARING.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">You have rejected Mr Def Defendant's response and want to proceed to court."
                               + " If the case goes to a hearing we will contact you with further details.</p><p class=\"govuk-body\">"
                               + "<a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  "
                               + "rel=\"noopener noreferrer\" class=\"govuk-link\">View the defendant's response</a>.</p>"),
                jsonPath("$[0].titleCy").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">You have rejected Mr Def Defendant's response and want to proceed to court."
                               + " If the case goes to a hearing we will contact you with further details.</p><p class=\"govuk-body\">"
                               + "<a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  "
                               + "rel=\"noopener noreferrer\" class=\"govuk-link\">View the defendant's response</a>.</p>")
            );
    }
}
