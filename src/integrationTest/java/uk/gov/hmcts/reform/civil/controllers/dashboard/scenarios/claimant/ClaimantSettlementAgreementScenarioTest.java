package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimantSettlementAgreementScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_claimant_settlement_agreement_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        LocalDate respondent1SettlementDeadline = OffsetDateTime.now().toLocalDate();
        doPost(BEARER_TOKEN,
                ScenarioRequestParams.builder()
                        .params(
                                Map.of(
                                        "claimantSettlementAgreement", "accepted",
                                        "respondent1SettlementAgreementDeadlineEn", respondent1SettlementDeadline,
                                        "respondent1SettlementAgreementDeadlineCy", respondent1SettlementDeadline
                                )
                        )
                        .build(),
                DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT.getScenario(), caseId
        )
                .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
                .andExpect(status().isOk())
                .andExpectAll(
                        status().is(HttpStatus.OK.value()),
                        jsonPath("$[0].titleEn").value("Settlement agreement"),
                        jsonPath("$[0].descriptionEn").value(
                                "<p class=\"govuk-body\">You have accepted the defendant's plan and asked them to sign a settlement agreement.</p>" +
                                        "<p class=\"govuk-body\">The defendant must respond by " + respondent1SettlementDeadline + ".</p>" +
                                        "<p class=\"govuk-body\">If they do not respond by then, " +
                                        "or reject the agreement, you can request a County Court Judgment.</p>"),
                        jsonPath("$[0].titleCy").value("Settlement agreement"),
                        jsonPath("$[0].descriptionCy").value(
                                "<p class=\"govuk-body\">You have accepted the defendant's plan and asked them to sign a settlement agreement.</p>" +
                                        "<p class=\"govuk-body\">The defendant must respond by " + respondent1SettlementDeadline + ".</p>" +
                                        "<p class=\"govuk-body\">If they do not respond by then, " +
                                        "or reject the agreement, you can request a County Court Judgment.</p>"));
    }
}
