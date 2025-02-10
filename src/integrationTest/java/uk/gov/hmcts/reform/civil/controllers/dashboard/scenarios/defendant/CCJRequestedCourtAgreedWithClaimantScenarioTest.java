package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantCCJResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CCJRequestedCourtAgreedWithClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    ClaimantCCJResponseDefendantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_court_agreed_with_claimant_ccj_requested_scenario() throws Exception {

        String caseId = "994321234";
        String claimantFirstName = "John";
        String claimantLastName = "Smith";
        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
            .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
            .ccjPaymentPaidSomeOption(YesOrNo.YES)
            .build();
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.CASE_SETTLED)
            .ccjPaymentDetails(ccjPaymentDetails)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .specRespondent1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .applicant1(Party.builder()
                            .individualFirstName(claimantFirstName)
                            .individualLastName(claimantLastName)
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .respondent1(Party.builder()
                             .individualFirstName(claimantFirstName)
                             .individualLastName(claimantLastName)
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .claimantCourtDecision(RepaymentDecisionType
                                                                                   .IN_FAVOUR_OF_CLAIMANT).build())
                             .build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value(claimantFirstName + " " + claimantLastName
                                                   + " has requested a County Court Judgment against you"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">John Smith rejected your "
                        + "<a href=\"{CCJ_REPAYMENT_PLAN_DEFENDANT_URL}\" class=\"govuk-link\">repayment plan</a> "
                        + "and has proposed a new plan, which the court agreed with, based on the financial details you"
                        + " provided.</p> <p class=\"govuk-body\">When we've processed the request, we'll post a copy "
                        + "of the judgment to you.</p><p class=\"govuk-body\">If you pay the debt within one month of"
                        + " the date of judgment, the County Court Judgment (CCJ) is removed from the public register."
                        + " You can pay £15 to <a href={APPLY_FOR_CERTIFICATE} class=\"govuk-link\" target=\"_blank\""
                        + " rel=\"noopener noreferrer\"> apply for a certificate (opens in new tab)</a> that confirms"
                        + " this.</p><p class=\"govuk-body\"><a href=\"{CITIZEN_CONTACT_THEM_URL}\" "
                        + "class=\"govuk-link\">Contact " + claimantFirstName + " " + claimantLastName + "</a> if you "
                        +
                        "need their payment details.</p> <p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  "
                        + "rel=\"noopener noreferrer\" class=\"govuk-link\">View your response</a></p>"
                )
            );
    }
}
