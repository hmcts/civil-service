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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_PART_REMISSION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimIssueHwFPartRemissionGrantedScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_claim_issue_hwf_part_remission_scenario() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("claimIssueRemissionAmount", "£1000");
        map.put("claimIssueOutStandingAmount", "£25");
        UUID caseId = UUID.randomUUID();

        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder()
                .params(map).build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA6_CLAIM_ISSUE_HWF_PART_REMISSION.getScenario(),
            caseId
        ).andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "Your help with fees application has been reviewed"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You'll get help with the claim fee. £1000 will be covered by fee remission.<br>You must still pay the remaining fee of £25. " +
                    "You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>"),
            jsonPath("$[0].titleCy").value(
                "Mae eich cais am help i dalu ffioedd wedi cael ei adolygu"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Byddwch yn cael help gyda ffi’r hawliad. Bydd y swm o £1000 yn cael ei ddileu.<br>" +
                    "Bydd rhaid i chi dal dalu’r ffi sy’n weddill o £25. Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}.</p>")

        );
    }
}
