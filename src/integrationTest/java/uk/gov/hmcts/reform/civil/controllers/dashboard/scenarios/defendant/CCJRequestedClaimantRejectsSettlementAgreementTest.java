package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.CCJRequestedDashboardNotificationDefendantHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

public class CCJRequestedClaimantRejectsSettlementAgreementTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CCJRequestedDashboardNotificationDefendantHandler handler;

    @Test
    void should_create_scenario_for_defendant_ccj_requested_defendant_rejects_settlement_agreement() throws Exception {

        String caseId = "13076";
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .ccdCaseReference(Long.valueOf(caseId))
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder().applicant1SignedSettlementAgreement(YesOrNo.YES).build())
                             .respondentSignSettlementAgreement(YesOrNo.NO)
                             .build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mr. John Rambo has requested a County Court Judgment against you"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">The claimant rejected your repayment plan and asked you to sign a settlement. You did not sign the agreement.</p>" +
                            "<p class=\"govuk-body\">When we’ve processed the request, we’ll post a copy of the judgment to you.</p>" +
                            "<p class=\"govuk-body\">If you pay the debt within one month of the date of judgment, the County Court Judgment (CCJ) " +
                            "is removed from the public register. You can pay £15 to <a href={APPLY_FOR_CERTIFICATE} " +
                            "class=\"govuk-link\" target=\"_blank\" rel=\"noopener noreferrer\">apply for a certificate (opens in new tab)</a> " +
                            "that confirms this.</p><p class=\"govuk-body\"><a href=\"{CITIZEN_CONTACT_THEM_URL}\" class=\"govuk-link\">Contact Mr. John Rambo</a> " +
                            "if you need their payment details.<br><a href={VIEW_DEFENDANT_RESPONSE} class=\"govuk-link\">View your response</a></p>"),
                jsonPath("$[0].titleCy").value("Mr. John Rambo has requested a County Court Judgment against you"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">The claimant rejected your repayment plan and asked you to sign a settlement. You did not sign the agreement.</p>" +
                            "<p class=\"govuk-body\">When we’ve processed the request, we’ll post a copy of the judgment to you.</p>" +
                            "<p class=\"govuk-body\">If you pay the debt within one month of the date of judgment, the County Court Judgment (CCJ) " +
                            "is removed from the public register. You can pay £15 to <a href={APPLY_FOR_CERTIFICATE} " +
                            "class=\"govuk-link\" target=\"_blank\" rel=\"noopener noreferrer\">apply for a certificate (opens in new tab)</a> " +
                            "that confirms this.</p><p class=\"govuk-body\"><a href=\"{CITIZEN_CONTACT_THEM_URL}\" class=\"govuk-link\">Contact Mr. John Rambo</a> " +
                            "if you need their payment details.<br><a href={VIEW_DEFENDANT_RESPONSE} class=\"govuk-link\">View your response</a></p>")

            );
    }
}
