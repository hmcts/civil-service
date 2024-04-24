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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_INVALID_REF;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimIssueHwfInvalidRefScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_claim_issue_hwf_invalid_ref_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder().params(new HashMap<>(Map.of("typeOfFee", "claim"))).build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA6_CLAIM_ISSUE_HWF_INVALID_REF.getScenario(),
            caseId
        ).andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value("You've provided an invalid help with fees reference number"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You've applied for help with the claim fee, but the reference"
                    + " number is invalid.<br>You've been sent an email with instructions on what to do next."
                    + " If you've already read the email and taken action, you can disregard this message.<br>You can pay by"
                    + " phone by calling {civilMoneyClaimsTelephone}.</p>"),
            jsonPath("$[0].titleCy").value("Rydych wedi darparu cyfeirnod help i dalu ffioedd annilys"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Rydych wedi gwneud cais am help i dalu ffi’r hawliad, ond mae'r cyfeirnod yn annily."
                    + "<br>Anfonwyd e-bost atoch gyda chyfarwyddiadau ar beth i'w wneud nesaf. Os ydych eisoes wedi darllen yr e-bost ac wedi gweithredu,"
                    + " gallwch anwybyddu'r neges hon.<br>Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephoneWelshSpeaker}.</p>")

        );
    }

}
