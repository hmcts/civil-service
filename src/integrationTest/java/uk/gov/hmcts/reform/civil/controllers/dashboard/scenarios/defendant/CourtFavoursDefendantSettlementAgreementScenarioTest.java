package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
public class CourtFavoursDefendantSettlementAgreementScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_scenario_for_court_favours_defendant_sign_settlement_agreement() throws Exception {

        UUID caseId = UUID.randomUUID();

        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(Map.of("respondSettlementAgreementDeadline", "16 March 2024"))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL,
               "Scenario.AAA7.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant",
               caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Settlement agreement"),
                jsonPath("$[0].titleCy").value("Settlement agreement"),
                jsonPath("$[0].descriptionEn")
                    .value("The claimant has rejected your plan and asked you to sign a settlement agreement.<br><br>"
                               + "The claimant has proposed a new repayment plan and the court has agreed with it, based on the financial details you provided.<br><br>"
                               + "You must respond by 16 March 2024. If you do not respond by then, or reject the agreement, they can request a County Court Judgment.<br><br>"
                               + "<a href=\"{VIEW_REPAYMENT_PLAN}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">View the repayment plan</a><br>"
                               + "<a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">View your response</a>"),
                jsonPath("$[0].descriptionCy")
                    .value("The claimant has rejected your plan and asked you to sign a settlement agreement.<br><br>"
                               + "The claimant has proposed a new repayment plan and the court has agreed with it, based on the financial details you provided.<br><br>"
                               + "You must respond by 16 March 2024. If you do not respond by then, or reject the agreement, they can request a County Court Judgment.<br><br>"
                               + "<a href=\"{VIEW_REPAYMENT_PLAN}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">View the repayment plan</a><br>"
                               + "<a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">View your response</a>")
                );
    }
}
