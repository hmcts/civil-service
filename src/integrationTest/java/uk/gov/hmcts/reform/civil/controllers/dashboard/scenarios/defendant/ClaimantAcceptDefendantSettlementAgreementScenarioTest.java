package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimantAcceptDefendantSettlementAgreementScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_scenario_for_claimant_accept_defendant_plan_settlement_agreement() throws Exception {
        UUID caseId = UUID.randomUUID();
        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder()
                .params(new HashMap<>(Map.of("respondent1SettlementAgreementDeadline_En", "16 March 2024",
                                             "respondent1SettlementAgreementDeadline_Cy", "16 March 2024"
                )))
                .build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT.getScenario(),
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
                    .value(
                        "<p class=\"govuk-body\">The claimant has accepted your plan and asked you to sign a settlement agreement."
                            + " You must respond by 16 March 2024.</p><p class=\"govuk-body\">If you do not respond by then, or reject the agreement,"
                            + " they can request a County Court Judgment.</p><p class=\"govuk-body\"><a href=\"{VIEW_REPAYMENT_PLAN}\""
                            + " rel=\"noopener noreferrer\" class=\"govuk_link\">View the repayment plan</a><br><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" "
                            + "rel=\"noopener noreferrer\" class=\"govuk_link\">View your response</a></p>"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">The claimant has accepted your plan and asked you to sign a settlement agreement."
                            + " You must respond by 16 March 2024.</p><p class=\"govuk-body\">If you do not respond by then, or reject the agreement,"
                            + " they can request a County Court Judgment.</p><p class=\"govuk-body\"><a href=\"{VIEW_REPAYMENT_PLAN}\""
                            + " rel=\"noopener noreferrer\" class=\"govuk_link\">View the repayment plan</a><br><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" "
                            + "rel=\"noopener noreferrer\" class=\"govuk_link\">View your response</a></p>")
            );
    }
}
