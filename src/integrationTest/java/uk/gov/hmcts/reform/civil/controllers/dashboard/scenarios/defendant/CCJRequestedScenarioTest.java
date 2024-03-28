package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.CCJRequestedDashboardNotificationHandler;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class CCJRequestedScenarioTest extends BaseIntegrationTest {

    @Autowired
    private CCJRequestedDashboardNotificationHandler handler;

    @Test
    void should_create_ccj_requested_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        String claimantName = "Dave Indent";
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(
                       Map.of(
                           "applicant1PartyName", claimantName
                       )
                   )
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Claimant has requested a County Court Judgment (CCJ)"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">" + claimantName + " has requested CCJ against you, because the response deadline has passed.</p>"
                        + "<p class=\"govuk-body\">Your online account will not be updated with the progress of the claim, and any further updates will be by post.</p>"
                        + "<p class=\"govuk-body\">If your deadline has passed, but the CCJ has not been issued, you can still respond. " +
                        "Get in touch with HMCTS on {civilMoneyClaimsTelephone} if you are in England and Wales, or 0300 790 6234 if you are in Scotland. " +
                        "You can call from Monday to Friday, between 8.30am to 5pm. <a href=\"https://www.gov.uk/call-charges\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">Find out about call charges (opens in new tab).</a></p>"
                        + "<p class=\"govuk-body\">If you do not get in touch, we will post a CCJ to you and <Name> and explain what to do next.</p>"));

    }

}
