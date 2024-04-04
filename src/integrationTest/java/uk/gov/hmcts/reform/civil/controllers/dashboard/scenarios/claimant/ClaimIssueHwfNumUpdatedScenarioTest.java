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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_UPDATED;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimIssueHwfNumUpdatedScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_claimIssue_hwf_num_updated_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
                ScenarioRequestParams.builder()
                        .params(new HashMap<>(Map.of("typeOfFee", "claim")))
                        .build(),
                DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA6_CLAIM_ISSUE_HWF_UPDATED.getScenario(), caseId
        )
             .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
                .andExpect(status().isOk())
                .andExpectAll(
                        status().is(HttpStatus.OK.value()),
                        jsonPath("$[0].titleEn").value("Your help with fees application has been updated"),
                        jsonPath("$[0].descriptionEn")
                                .value("<p class=\"govuk-body\">You've applied for help with the claim fee. You'll receive an update from us within 5 to 10 working days.</p>"),
                        jsonPath("$[0].titleCy").value("Your help with fees application has been updated"),
                        jsonPath("$[0].descriptionCy")
                                .value("<p class=\"govuk-body\">You've applied for help with the claim fee. You'll receive an update from us within 5 to 10 working days.</p>"));
    }
}
