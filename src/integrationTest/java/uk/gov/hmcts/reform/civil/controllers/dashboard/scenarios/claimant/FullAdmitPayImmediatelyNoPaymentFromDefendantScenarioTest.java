package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_FULL_ADMIT_CLAIMANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class FullAdmitPayImmediatelyNoPaymentFromDefendantScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_full_admit_pay_immediately_no_payment_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        LocalDate respondent1AdmittedAmountPaymentDeadline = OffsetDateTime.now().toLocalDate();
        String defendantName = "Dave Indent";
        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder().params(new HashMap<>(Map.of(
                "respondent1PartyName", defendantName,
                "respondent1AdmittedAmountPaymentDeadlineEn", respondent1AdmittedAmountPaymentDeadline,
                "respondent1AdmittedAmountPaymentDeadlineCy", respondent1AdmittedAmountPaymentDeadline
                ))).build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA6_CLAIMANT_INTENT_FULL_ADMIT_CLAIMANT.getScenario(),
            caseId
        ).andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value("Immediate payment"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You have accepted Dave Indent's plan to pay £{fullAdmitPayImmediatelyPaymentAmount} "
                    + "immediately. Funds must clear your account by " + respondent1AdmittedAmountPaymentDeadline + ".</p><p class=\"govuk-body\">If you don't receive"
                    + " the money by then, you can <a href=\"{COUNTY_COURT_JUDGEMENT_URL}\"  "
                    + "rel=\"noopener noreferrer\" class=\"govuk-link\">request a County Court Judgment</a>.</p>"),
            jsonPath("$[0].titleCy").value("Immediate payment"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">You have accepted Dave Indent's plan to pay £{fullAdmitPayImmediatelyPaymentAmount} "
                    + "immediately. Funds must clear your account by " + respondent1AdmittedAmountPaymentDeadline + ".</p><p class=\"govuk-body\">If you don't receive "
                    + "the money by then, you can <a href=\"{COUNTY_COURT_JUDGEMENT_URL}\" "
                    + " rel=\"noopener noreferrer\" class=\"govuk-link\">request a County Court Judgment</a>.</p>")

        );
    }

}
