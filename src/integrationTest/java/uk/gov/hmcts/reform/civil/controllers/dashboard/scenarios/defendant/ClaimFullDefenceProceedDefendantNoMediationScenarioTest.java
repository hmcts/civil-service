package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimFullDefenceProceedDefendantNoMediationScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_scenario_for_claim_settled() throws Exception {

        UUID caseId = UUID.randomUUID();
        String claimantName = "Clay Mint";

        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>(Map.of("applicant1PartyName", claimantName
                   )))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, "Scenario.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant", caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">" + claimantName + " wants to proceed with the claim.</p>" +
                               "<p class=\"govuk-body\">The case will be referred to a judge who will decide what should happen next.</p>" +
                               "<p class=\"govuk-body\">You can <a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">view your response</a> or " +
                               "<a href=\"{VIEW_CLAIMANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">view the claimant's hearing requirements</a>.</p>")
            );
    }
}
