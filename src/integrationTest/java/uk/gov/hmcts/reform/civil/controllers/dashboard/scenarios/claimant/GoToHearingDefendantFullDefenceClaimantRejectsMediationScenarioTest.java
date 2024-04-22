package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class GoToHearingDefendantFullDefenceClaimantRejectsMediationScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_go_to_hearing_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>(Map.of("applicant1PartyName", "John Doe")))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">John Doe wants to proceed to court.</p>"
                               + " <p class=\"govuk-body\">They rejected your response.</p> <p class=\"govuk-body\">They"
                               + " said no to mediation.</p> <p class=\"govuk-body\">If the case goes to a hearing we will"
                               + " contact you with further details.</p> <p class=\"govuk-body\"><a"
                               + " href={VIEW_DEFENDANT_RESPONSE} class=\"govuk-link\">View your response</a><br>"
                               + "<a target=\"_blank\" href={VIEW_CLAIMANT_HEARING_REQS} rel=\"noopener noreferrer\""
                               + " class=\"govuk-link\">View the claimant's hearing requirements</a></p>"),
                jsonPath("$[0].titleCy").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">John Doe wants to proceed to court.</p>"
                               + " <p class=\"govuk-body\">They rejected your response.</p> <p class=\"govuk-body\">They"
                               + " said no to mediation.</p> <p class=\"govuk-body\">If the case goes to a hearing we will"
                               + " contact you with further details.</p> <p class=\"govuk-body\"><a"
                               + " href={VIEW_DEFENDANT_RESPONSE} class=\"govuk-link\">View your response</a><br>"
                               + "<a target=\"_blank\" href={VIEW_CLAIMANT_HEARING_REQS} rel=\"noopener noreferrer\""
                               + " class=\"govuk-link\">View the claimant's hearing requirements</a></p>")
            );
    }
}
