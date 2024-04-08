package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class JudicialReferralFullDefencePartAdmitPaidPartialScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_scenario_for_judicial_referral_full_defence_or_part_admit_paid_partial_amount() throws Exception {

        UUID caseId = UUID.randomUUID();

        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder()
                .params(new HashMap<>(Map.of("applicant1PartyName", "Mr Claim Claimant",
                                             "admissionPaidAmount", "£3000"
                )))
                .build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            "Scenario.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant",
            caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for the court to review the case"),
                jsonPath("$[0].titleCy").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">Mr Claim Claimant wants to proceed to court.</p>"
                               + "<p class=\"govuk-body\">They rejected your admission of £3000 although they accept you have already paid it.</p>"
                               + "<p class=\"govuk-body\">If the case goes to a hearing we will contact you with further details.</p>"
                               + "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">View your response</a><br>"
                               + "<a target=\"_blank\" href=\"{VIEW_CLAIMANT_HEARING_REQS}\"  rel=\"noopener noreferrer\" "
                               + "class=\"govuk-link\">View the claimant's hearing requirements</a></p>"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">Mr Claim Claimant wants to proceed to court.</p>"
                               + "<p class=\"govuk-body\">They rejected your admission of £3000 although they accept you have already paid it.</p>"
                               + "<p class=\"govuk-body\">If the case goes to a hearing we will contact you with further details.</p>"
                               + "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">View your response</a><br>"
                               + "<a target=\"_blank\" href=\"{VIEW_CLAIMANT_HEARING_REQS}\"  rel=\"noopener noreferrer\" "
                               + "class=\"govuk-link\">View the claimant's hearing requirements</a></p>")
            );
    }
}
