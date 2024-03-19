package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIM_ISSUE_RESPONSE_AWAIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_DEFENDANT_ADMIT_PAY_IMMEDIATELY_CLAIMANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class DefendantResponsePartAdmitPayImmediatelyClaimantScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_part_admit_defendant_response_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(
                       Map.of(
                       "respondent1AdmittedAmountPaymentDeadlineEn", "18 March 2024",
                       "respondent1AdmittedAmountPaymentDeadlineCy", "18 March 2024",
                       "defendantAdmittedAmount", "£300"
                       )
                   )
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA7_DEFENDANT_ADMIT_PAY_IMMEDIATELY_CLAIMANT.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value("<p class=\"govuk-body\">The defendant has offered to pay £300 by 18 March 2024.<br>The payment must clear the account by then, if not you can request a county court judgment.<br><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">View and respond</a>.</p>"),
                jsonPath("$[0].descriptionCy").value("<p class=\"govuk-body\">The defendant has offered to pay £300 by 18 March 2024.<br>The payment must clear the account by then, if not you can request a county court judgment.<br><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">View and respond</a>.</p>")
            );
    }

}
