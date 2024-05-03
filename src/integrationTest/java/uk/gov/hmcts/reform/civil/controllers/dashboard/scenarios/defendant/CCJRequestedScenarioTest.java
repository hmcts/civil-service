package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.CCJRequestedDashboardNotificationDefendantHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CCJRequestedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CCJRequestedDashboardNotificationDefendantHandler handler;

    @Test
    void should_create_ccj_requested_scenario() throws Exception {

        String caseId = "12348991012";

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Claimant has requested a County Court Judgment (CCJ)"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Mr. John Rambo has requested CCJ against you, because the response deadline has passed.</p>"
                        + "<p class=\"govuk-body\">Your online account will not be updated with the progress of the claim, and any further updates will be by post.</p>"
                        + "<p class=\"govuk-body\">If your deadline has passed, but the CCJ has not been issued, you can still respond. " +
                        "Get in touch with HMCTS on {civilMoneyClaimsTelephone} if you are in England and Wales, or 0300 790 6234 if you are in Scotland. " +
                        "You can call from Monday to Friday, between 8.30am to 5pm. <a href=\"https://www.gov.uk/call-charges\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">Find out about call charges (opens in new tab).</a></p>"
                        + "<p class=\"govuk-body\">If you do not get in touch, we will post a CCJ to you and <Name> and explain what to do next.</p>")
            );

    }

}
