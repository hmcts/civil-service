package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class MediationScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_mediation_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>())
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA6_CLAIMANT_MEDIATION.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You have said you wish continue with your claim"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">Your case will be referred for mediation. Your mediation appointment will be arranged within 28 days.</p><p class=\"govuk-body\"><a href=\"https://www.gov.uk/guidance/small-claims-mediation-service\"  rel=\"noopener noreferrer\" class=\"govuk-link\"> Find out more about how mediation works (opens in new tab)</a>.</p>"),
                jsonPath("$[0].titleCy").value("You have said you wish continue with your claim"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Your case will be referred for mediation. Your mediation appointment will be arranged within 28 days.</p><p class=\"govuk-body\"><a href=\"https://www.gov.uk/guidance/small-claims-mediation-service\"  rel=\"noopener noreferrer\" class=\"govuk-link\"> Find out more about how mediation works (opens in new tab)</a>.</p>")
            );
    }
}
