package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimFullDefenceProceedDefendantNoMediationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_scenario_for_claim_settled() throws Exception {

        String caseId = "12348991013";

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentPartAdmissionSpec().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">Mr. John Rambo wants to proceed to court.</p>" +
                               "<p class=\"govuk-body\">They rejected your response.</p>" +
                               "<p class=\"govuk-body\">If the case goes to a hearing we will contact you with further details.</p>" +
                               "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">View your response</a><br>" +
                               "<a href=\"{VIEW_CLAIMANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">View the claimant's hearing requirements</a></p>")
            );
    }
}
