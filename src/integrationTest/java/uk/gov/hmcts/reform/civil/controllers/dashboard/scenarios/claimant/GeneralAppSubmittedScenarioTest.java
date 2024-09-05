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

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class GeneralAppSubmittedScenarioTest extends BaseIntegrationTest {
    public static final String SCENARIO_GA_SUBMITTED = "Scenario.AAA6.GeneralApps.ApplicationSubmitted.Applicant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";
    private static final String UPDATE_DASHBOARD_NOTIFICATION = "/dashboard/notifications/{ccd-case-identifier}/role/{role-type}";

    @Test
    void should_create_scenario_for_general_application_submitted() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_SUBMITTED, caseId
        )
            .andExpect(status().isOk());

        //Verify Dashboard Notification is created
        doGet(BEARER_TOKEN, UPDATE_DASHBOARD_NOTIFICATION, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].template_name").value(
                    "<a href={GA_VIEW_APPLICATION_URL} rel=\"noopener noreferrer\" class=\"govuk-link\">View application documents</a>"),
                jsonPath("$[0].currentStatusEn").value("Available")
            );
    }
}
