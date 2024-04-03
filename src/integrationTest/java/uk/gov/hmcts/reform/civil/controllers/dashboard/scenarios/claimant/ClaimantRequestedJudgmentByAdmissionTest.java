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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimantRequestedJudgmentByAdmissionTest extends BaseIntegrationTest {

    @Test
    void should_create_claimant_requested_judgment_by_admission() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("respondent1PartyName", "Mr Defendant");
        map.put("claimantRepaymentPlanDecision", "accepted");
        UUID caseId = UUID.randomUUID();
        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder()
                .params(map).build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT.getScenario(),
            caseId
        ).andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "You requested a County Court Judgment against Mr Defendant"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You accepted the <a href=\"{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}\" class=\"govuk-link\">repayment plan.</a></p><p class=\"govuk-body\">When we've processed the request, we'll post a copy of the judgment to you.</p><p class=\"govuk-body\"><a href=\"{TELL_US_IT_IS_SETTLED}\" rel=\"noopener noreferrer\" class=\"govuk-link\">Tell us it's paid</a></p>"),
            jsonPath("$[0].titleCy").value(
                "You requested a County Court Judgment against Mr Defendant"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">You accepted the <a href=\"{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}\" class=\"govuk-link\">repayment plan.</a></p><p class=\"govuk-body\">When we've processed the request, we'll post a copy of the judgment to you.</p><p class=\"govuk-body\"><a href=\"{TELL_US_IT_IS_SETTLED}\" rel=\"noopener noreferrer\" class=\"govuk-link\">Tell us it's paid</a></p>")
        );
    }
}


