package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_MEDIATION_SUCCESSFUL;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class DefendantMediationSuccessfulDashboardNotificationScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_mediation_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(Map.of())
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA6_DEFENDANT_MEDIATION_SUCCESSFUL.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mediation appointment successful"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">Both parties attended mediation and an agreement was reached.</p> "
                            + "<p class=\"govuk-body\">This case is now settled and no further action is needed.</p> "
                            + "<p class=\"govuk-body\">You can view your mediation agreement <a href=\"{MEDIATION_SUCCESSFUL_URL}\" "
                            + "rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">here</a>.</p>"),
                jsonPath("$[0].titleCy").value("Mediation appointment successful"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Both parties attended mediation and an agreement was reached.</p> "
                            + "<p class=\"govuk-body\">This case is now settled and no further action is needed.</p> "
                            + "<p class=\"govuk-body\">You can view your mediation agreement <a href=\"{MEDIATION_SUCCESSFUL_URL}\" "
                            + "rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">here</a>.</p>")
            );
    }
}
