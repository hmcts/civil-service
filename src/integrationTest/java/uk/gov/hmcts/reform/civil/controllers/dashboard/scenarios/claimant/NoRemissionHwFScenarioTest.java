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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_NO_REMISSION;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class NoRemissionHwFScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_no_remission_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        String claimFee = "100";
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(
                       new HashMap<>(Map.of(
                           "claimFee", claimFee
                       ))
                   )
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA6_CLAIM_ISSUE_HWF_NO_REMISSION.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn")
                    .value("Your help with fees application has been rejected"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">We've rejected your application for help with the claim fee. See the email for "
                        + "further details.</p><p class=\"govuk-body\">You must pay the full fee of "
                        + claimFee + ". You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>"),
                jsonPath("$[0].titleCy")
                    .value("Mae eich cais am help i dalu ffioedd wedi cael ei wrthod"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydym wedi gwrthod eich cais am help i dalu ffi’r hawliad. Gweler yr e-bost am ragor o fanylion.</p>" +
                        "<p class=\"govuk-body\">Rhaid i chi dalu’r ffi lawn o "
                        + claimFee + ". Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}.</p>")
            );

    }

}
