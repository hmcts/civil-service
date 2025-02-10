package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class GeneralAppCompleteScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_COMPLETE = "Scenario.AAA6.GeneralApplication.Complete.Defendant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";
    private static final String GET_TASKS_ITEMS_URL = "/dashboard/taskList/{ccd-case-identifier}/role/{role-type}";

    @Test
    @DirtiesContext
    void should_create_scenario_for_general_application_complete() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_COMPLETE, caseId
        )
            .andExpect(status().isOk());

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel=\"noopener noreferrer\" class=\"govuk-link\">View applications</a>"),
                jsonPath("$[0].currentStatusEn").value("Available")
            );
    }

}
