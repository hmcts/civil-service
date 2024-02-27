package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios;

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

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimantAfterPayFeeScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_DRAFT_CLAIM = "Scenario.AAA7.ClaimIssue.Response.Await";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";
    private static final String GET_NOTIFICATIONS_URL
        = "/dashboard/notifications/{ccd-case-identifier}/role/{role-type}";

    @Test
    void should_create_draft_claim_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(Map.of())
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_DRAFT_CLAIM, caseId
        )
            .andExpect(status().isOk());


        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for defendant to respond"),
                jsonPath("$[0].descriptionEn").value("${claimantName} has until <Date> to respond. They can request an extra 28 days if they need it.")
            );
    }

}
