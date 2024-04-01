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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimRejectedNotPaidScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_scenario_for_claimant_reject_for_not_paid_defendant_notification() throws Exception {
        UUID caseId = UUID.randomUUID();
        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder()
                .params(Map.of("applicant1PartyName", "Test Applicant",
                               "claimSettledAmount", "£1000"
                ))
                .build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT.getScenario(),
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
                    .value(
                        "<p class=\"govuk-body\">Test Applicant wants to proceed to court.</p>"
                            + "<p class=\"govuk-body\">They said you have not paid the £1000 you admit you owe.</p>"
                            + "<p class=\"govuk-body\">If the case goes to a hearing we will contact you with further details.</p>"
                            + "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" rel=\"noopener noreferrer\" class=\"govuk-link\">View your response</a><br>"
                            + "<a target=\"_blank\" href=\"{VIEW_CLAIMANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\">View the claimant's hearing requirements</a></p>"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Test Applicant wants to proceed to court.</p>"
                            + "<p class=\"govuk-body\">They said you have not paid the £1000 you admit you owe.</p>"
                            + "<p class=\"govuk-body\">If the case goes to a hearing we will contact you with further details.</p>"
                            + "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" rel=\"noopener noreferrer\" class=\"govuk-link\">View your response</a><br>"
                            + "<a target=\"_blank\" href=\"{VIEW_CLAIMANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\">View the claimant's hearing requirements</a></p>")
            );
    }
}
