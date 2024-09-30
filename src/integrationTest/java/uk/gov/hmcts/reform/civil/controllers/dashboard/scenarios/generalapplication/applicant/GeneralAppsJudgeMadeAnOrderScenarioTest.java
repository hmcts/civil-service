package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.generalapplication.applicant;

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

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class GeneralAppsJudgeMadeAnOrderScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_ORDER_MADE = "Scenario.AAA6.GeneralApps.OrderMade.Applicant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    void should_create_scenario_for_general_application_an_order_made() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_ORDER_MADE, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "APPLICANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("An order has been made"),
                jsonPath("$[0].titleCy").value("Mae gorchymyn wedi’i wneud"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The judge has made an order related to the application. <a href=\"{GA_VIEW_APPLICATION_URL}\" class=\"govuk-link\">View the order</a></p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae'r barnwr wedi gwneud gorchymyn yn ymwneud â'r cais. <a href=\"{GA_VIEW_APPLICATION_URL}\" class=\"govuk-link\">Gweld y gorchymyn</a></p>")
            );
    }
}
