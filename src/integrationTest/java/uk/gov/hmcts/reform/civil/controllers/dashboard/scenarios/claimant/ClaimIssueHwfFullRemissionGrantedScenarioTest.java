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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIM_ISSUE_HWF_FULL_REMISSION;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimIssueHwfFullRemissionGrantedScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_claim_issue_hwf_full_remission_scenario() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("claimFee", "£455");
        UUID caseId = UUID.randomUUID();

        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder()
                .params(map).build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA7_CLAIM_ISSUE_HWF_FULL_REMISSION.getScenario(),
            caseId
        ).andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "Your help with fees application has been reviewed"),
            jsonPath("$[0].descriptionEn").value(
                "The full claim fee of £455 will be covered. You do not need to make a payment."),
            jsonPath("$[0].titleCy").value(
                "Your help with fees application has been reviewed"),
            jsonPath("$[0].descriptionCy").value(
                "The full claim fee of £455 will be covered. You do not need to make a payment.")
        );
    }
}
