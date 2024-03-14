package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;


import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_DEFENDANT_RESPONSE_ACCEPTS_CLAIMANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class DefendantAcceptsSettlementClaimantNotificationTest extends BaseIntegrationTest {

    @Test
    void should_create_defendant_accepts_settlement_agreement() throws Exception {

        String defendantName = "Dave Indent";
        UUID caseId = UUID.randomUUID();
        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder()
                .params(
                    Map.of(
                        "respondent1PartyName", defendantName
                    )
                ).build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_DEFENDANT_RESPONSE_ACCEPTS_CLAIMANT.getScenario(),
            caseId
        ).andExpect(status().isOk());


        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "Your help with fees application has been reviewed"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You'll get help with the claim fee. You'll receive £1000 towards it.<br> " +
                    "You must still pay the remaining fee of £25.You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>"),
            jsonPath("$[0].titleCy").value(
                "Your help with fees application has been reviewed"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">You'll get help with the claim fee. You'll receive £1000 towards it.<br> " +
                    "You must still pay the remaining fee of £25.You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>")

        );
    }

}
