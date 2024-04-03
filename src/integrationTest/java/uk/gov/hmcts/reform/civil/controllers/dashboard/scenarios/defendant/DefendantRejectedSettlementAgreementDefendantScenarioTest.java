package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_SETTLEMENT_AGREEMENT_DEFENDANT_REJECTED_DEFENDANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class DefendantRejectedSettlementAgreementDefendantScenarioTest extends BaseIntegrationTest {

    @Test
    void should_defendant_rejected_settlement_agreement_scenario() throws Exception {
        UUID caseId = UUID.randomUUID();
        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder().params(new HashMap<>()).build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA6_SETTLEMENT_AGREEMENT_DEFENDANT_REJECTED_DEFENDANT.getScenario(),
            caseId
        ).andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value("Settlement agreement"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You have rejected the settlement agreement.</p> " +
                    "<p class=\"govuk-body\">The claimant can request a County Court Judgment (CCJ), " +
                    "which would order you to repay the money in line with the agreement. " +
                    "The court believes you can afford this.</p> " +
                    "<p class=\"govuk-body\">If the claimant requests a CCJ then you can ask a judge " +
                    "to consider changing the plan, based on your financial details.</p>")
        );
    }

}
