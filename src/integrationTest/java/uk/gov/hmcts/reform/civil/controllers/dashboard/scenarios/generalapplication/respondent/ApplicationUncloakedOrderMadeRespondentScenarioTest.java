package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.generalapplication.respondent;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ApplicationUncloakedOrderMadeRespondentScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_APPLICATION_UNCLOAKED_ORDER_MADE_RESPONDENT = "Scenario.AAA6.GeneralApps.ApplicationUncloaked.OrderMade.Respondent";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    void should_create_scenario_for_respondent_when_order_made_and_application_uncloaked_submitted() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_APPLICATION_UNCLOAKED_ORDER_MADE_RESPONDENT, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "RESPONDENT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("An order has been made"),
                jsonPath("$[0].titleCy").value("Mae gorchymyn wedi’i wneud"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The other parties have requested a change to the case "
                        + "and the judge has made an order.</p><a href=\"{GA_RESPONSE_VIEW_APPLICATION_URL}\""
                        + " class=\"govuk-link\">View the request and order from the judge</a>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae’r partïon eraill wedi gofyn i newid gael ei wneud "
                        + "i'r achos ac mae’r barnwr wedi gwneud gorchymyn.</p>"
                        + "<a href=\"{GA_RESPONSE_VIEW_APPLICATION_URL}\" class=\"govuk-link\">Gweld y cais "
                        + "a’r gorchymyn gan y barnwr</a>")
            );
    }

}
