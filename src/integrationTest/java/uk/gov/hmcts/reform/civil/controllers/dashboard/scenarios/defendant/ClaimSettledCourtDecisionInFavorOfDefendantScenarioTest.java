package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimSettledCourtDecisionInFavorOfDefendantScenarioTest extends BaseIntegrationTest {

    @MockBean
    private OffsetDateTime time;

    @Test
    void should_create_scenario_for_claim_settle() throws Exception {

        UUID caseId = UUID.randomUUID();
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>(Map.of(
                       "respondent1SettlementAgreementDeadlineEn", responseDeadline
                   )))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL,
               SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Settlement agreement"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">The claimant has rejected your plan and asked you to "
                               + "sign a settlement agreement."
                               + "</p><p class=\"govuk-body\">"
                               + "The claimant proposed a repayment plan, and the court "
                               + "then responded with an alternative plan that was accepted."
                               + "</p><p class=\"govuk-body\">"
                               + " You must respond by " + responseDeadline + ". If you do not respond by then, "
                               + "or reject the agreement, they can request a County Court Judgment.</p><p"
                               + " class=\"govuk-body\"><a href=\"{VIEW_REPAYMENT_PLAN}\"  rel=\"noopener noreferrer\" "
                               + "class=\"govuk-link\">View the repayment plan</a><br><a "
                               + "href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener "
                               + "noreferrer\" class=\"govuk-link\">View your response</a></p>")
            );
    }
}
