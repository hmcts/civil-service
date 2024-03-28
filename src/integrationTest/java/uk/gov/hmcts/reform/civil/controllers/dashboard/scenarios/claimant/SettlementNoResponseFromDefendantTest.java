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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_NO_RESPONSE_CLAIMANT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class SettlementNoResponseFromDefendantTest extends BaseIntegrationTest {

    @Test
    void should_create_settlement_no_response_from_defendant() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("respondent1PartyName", "Mr Defendant");
        UUID caseId = UUID.randomUUID();
        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder()
                .params(map).build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_NO_RESPONSE_CLAIMANT.getScenario(),
            caseId
        ).andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "The defendant has not signed your settlement agreement"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You can <a href=\"{COUNTY_COURT_JUDGEMENT_URL}\" rel=\"noopener noreferrer\" class=\"govuk-link\">request a County Court Judgment</a> (CCJ), based on the repayment plan shown in the agreement.</p> <p class=\"govuk-body\">The court will make an order requiring them to pay the money. It does not guarantee that they pay it.</p> <p class=\"govuk-body\">Mr Defendant can still sign the settlement agreement until you request a CCJ.</p>"),
            jsonPath("$[0].titleCy").value(
                "The defendant has not signed your settlement agreement"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">You can <a href=\"{COUNTY_COURT_JUDGEMENT_URL}\" rel=\"noopener noreferrer\" class=\"govuk-link\">request a County Court Judgment</a> (CCJ), based on the repayment plan shown in the agreement.</p> <p class=\"govuk-body\">The court will make an order requiring them to pay the money. It does not guarantee that they pay it.</p> <p class=\"govuk-body\">Mr Defendant can still sign the settlement agreement until you request a CCJ.</p>")

        );
    }
}


