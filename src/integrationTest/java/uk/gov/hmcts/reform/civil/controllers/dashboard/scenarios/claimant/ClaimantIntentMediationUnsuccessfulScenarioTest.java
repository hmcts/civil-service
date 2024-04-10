package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantIntentMediationUnsuccessfulHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantIntentMediationUnsuccessfulScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantIntentMediationUnsuccessfulHandler handler;

    @Test
    void shouldCreateCcjRequestedScenario() throws Exception {

        String caseId = "323491";
        Party respondent1 = new Party();
        respondent1.toBuilder().partyName("John Doe").build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .ccdCaseReference(Long.valueOf(323491))
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().individualFirstName("John").individualLastName("Doe")
                             .type(Party.Type.INDIVIDUAL).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mediation was unsuccessful"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You weren't able to resolve your claim against John Doe using mediation. "
                        + "The court will review the case. We'll contact you to tell you what to do next.</p>"),
                jsonPath("$[0].titleCy").value("Mediation was unsuccessful"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">You weren't able to resolve your claim against John Doe using mediation. "
                        + "The court will review the case. We'll contact you to tell you what to do next.</p>"));

    }

}
