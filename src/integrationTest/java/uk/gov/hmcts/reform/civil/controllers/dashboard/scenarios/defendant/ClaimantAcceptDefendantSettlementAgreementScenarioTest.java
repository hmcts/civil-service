package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimantAcceptDefendantSettlementAgreementScenarioTest extends BaseIntegrationTest {

    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";
    private static final String GET_NOTIFICATIONS_URL
        = "/dashboard/notifications/{ccd-case-identifier}/role/{role-type}";
    private static final String GET_TASKS_ITEMS_URL = "/dashboard/taskList/{ccd-case-identifier}/role/{role-type}";

    @MockBean
    private OffsetDateTime time;

    @Test
    void should_create_scenario_for_claimant_accept_defendant_plan_settlement_agreement() throws Exception {

        UUID caseId = UUID.randomUUID();

        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder()
                .params(Map.of("respondent1SettlementAgreementDeadline_En", "16 March 2024"))
                .params(Map.of("respondent1SettlementAgreementDeadline_Cy", "16 March 2024"))
                .build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            "Scenario.AAA7.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant",
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
                            + " You must respond by 16 March 2024.</p><p class=\"govuk-body\">If you do not respond by then, or reject the agreement," +
                            " they can request a County Court Judgment.</p>"
                            + "<p class=\"govuk-body\"><a href=\"{VIEW_REPAYMENT_PLAN}\" rel=\"noopener noreferrer\" class=\"govuk-link\">View the repayment plan</a>"
                            + "<br><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" rel=\"noopener noreferrer\" class=\"govuk-link\">View your response</a></p>"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">The claimant has accepted your plan and asked you to sign a settlement agreement."
                            + " You must respond by 16 March 2024.</p><p class=\"govuk-body\">If you do not respond by then, or reject the agreement," +
                            " they can request a County Court Judgment.</p>"
                            + "<p class=\"govuk-body\"><a href=\"{VIEW_REPAYMENT_PLAN}\" rel=\"noopener noreferrer\" class=\"govuk-link\">View the repayment plan</a>"
                            + "<br><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" rel=\"noopener noreferrer\" class=\"govuk-link\">View your response</a></p>")
            );
    }
}
