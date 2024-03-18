package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_SETTLEMENT_AGREEMENT_DEFENDANT_ACCEPTED_DEFENDANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class DefendantAcceptSettlementAgreementDefendantScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_scenario_for_defendant_accept_defendant_plan_settlement_agreement() throws Exception {
        UUID caseId = UUID.randomUUID();
        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder()
                .params(Map.of(
                ))
                .build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA7_SETTLEMENT_AGREEMENT_DEFENDANT_ACCEPTED_DEFENDANT.getScenario(),
            caseId
        )
            .andExpect(status().isOk());
        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Settlement agreement"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">You have accepted the  "
                            + "<a href={VIEW_SETTLEMENT_AGREEMENT} target=\"_blank\" class=\"govuk-link\"> settlement"
                            + " agreement</a>. </p><p class=\"govuk-body\">The claimant cannot request a County Court "
                            + "Judgment, unless you break the terms of the agreement.</p>")
            );
    }
}
