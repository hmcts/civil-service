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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_INFO_REQUIRED;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimIssueHwfInfoRequiredScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_claim_issue_hwf_info_required_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder().params(new HashMap<>(Map.of("typeOfFee", "claim"))).build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA6_CLAIM_ISSUE_HWF_INFO_REQUIRED.getScenario(),
            caseId
        ).andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value("Your help with fees application needs more information"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">We need more information on your application for help with the claim fee.<br>You've been sent an email with further details." +
                    " If you've already read the email and taken action, you can disregard this message.<br>You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>"),
            jsonPath("$[0].titleCy").value("Mae angen i chi ddarparu mwy o wybodaeth am eich cais am help i dalu ffioedd"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Mae arnom angen mwy o wybodaeth am eich cais am help i dalu ffi’r hawliad.<br>Anfonwyd e-bost atoch gyda mwy o fanylion." +
                    " Os ydych eisoes wedi darllen yr e-bost ac wedi gweithredu, gallwch anwybyddu'r neges hon.<br>Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}.</p>")

        );
    }

}
