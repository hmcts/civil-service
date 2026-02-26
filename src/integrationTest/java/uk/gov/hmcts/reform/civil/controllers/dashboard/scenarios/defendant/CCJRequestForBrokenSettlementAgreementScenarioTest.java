package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.CCJRequestedDashboardNotificationDefendantHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CCJRequestForBrokenSettlementAgreementScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CCJRequestedDashboardNotificationDefendantHandler handler;

    @Test
    void should_create_upon_ccj_request_for_broken_settlement_for_accepted_repayment() throws Exception {

        String caseId = "1674364636586678";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(LocalDate.now().minusDays(1)))
            .respondent1RespondToSettlementAgreementDeadline(LocalDateTime.now().minusDays(1))
            .caseDataLiP(new CaseDataLiP().setRespondentSignSettlementAgreement(YesOrNo.YES)
                             .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                                                         .setApplicant1SignedSettlementAgreement(
                                                                             YesOrNo.YES)))
            .build();
        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mr. John Rambo has requested a County Court Judgment against you"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Mr. John Rambo accepted your repayment plan and asked you to sign a settlement. You signed the agreement but the claimant says you have broken the terms.</p><p class=\"govuk-body\">When we've processed the request, we'll post a copy of the judgment to you.</p> <p class=\"govuk-body\">If you pay the debt within one month of the date of judgment, the County Court Judgment (CCJ) is removed from the public register. You can pay £15 to <a href=\"{APPLY_FOR_CERTIFICATE}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">apply for a certificate (opens in new tab)</a> that confirms this.</p><p class=\"govuk-body\"><a href=\"{CITIZEN_CONTACT_THEM_URL}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">Contact Mr. John Rambo</a> if you need their payment details.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">View your response</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Mae Mr. John Rambo wedi gwneud cais am Ddyfarniad Llys Sirol yn eich erbyn"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae Mr. John Rambo wedi derbyn eich cynllun ad-dalu ac wedi gofyn i chi lofnodi setliad. " +
                        "Rydych wedi llofnodi’r cytundeb ond mae’r hawlydd yn dweud eich bod wedi torri’r telerau.</p>" +
                        "<p class=\"govuk-body\">Pan fyddwn wedi prosesu’r cais, byddwn yn anfon copi o’r dyfarniad drwy’r post atoch chi.</p>" +
                        "<p class=\"govuk-body\">Os byddwch yn talu’r ddyled o fewn mis o ddyddiad y dyfarniad, bydd y CCJ " +
                        "yn cael ei ddileu o’r gofrestr gyhoeddus. Gallwch dalu £15 i " +
                        "<a href=\"{APPLY_FOR_CERTIFICATE}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">" +
                        "wneud cais am dystysgrif (yn agor mewn ffenestr newydd)</a> sy’n cadarnhau hyn.</p><p class=\"govuk-body\">" +
                        "<a href=\"{CITIZEN_CONTACT_THEM_URL}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">Cysylltwch â Mr. John Rambo</a>" +
                        " os oes arnoch angen eu manylion talu.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld eich ymateb</a></p>")
            );
    }

    @Test
    void should_create_upon_ccj_request_for_broken_settlement_for_rejected_repayment() throws Exception {

        String caseId = "1674364636586678";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(LocalDate.now().minusDays(1)))
            .respondent1RespondToSettlementAgreementDeadline(LocalDateTime.now().minusDays(1))
            .caseDataLiP(new CaseDataLiP()
                             .setRespondentSignSettlementAgreement(YesOrNo.YES)
                             .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                                                         .setApplicant1SignedSettlementAgreement(
                                                                             YesOrNo.YES)))
            .build();
        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mr. John Rambo has requested a County Court Judgment against you"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Mr. John Rambo rejected your repayment plan and asked you to sign a settlement. You signed the agreement but the claimant says you have broken the terms.</p><p class=\"govuk-body\">When we've processed the request, we'll post a copy of the judgment to you.</p> <p class=\"govuk-body\">If you pay the debt within one month of the date of judgment, the County Court Judgment (CCJ) is removed from the public register. You can pay £15 to <a href=\"{APPLY_FOR_CERTIFICATE}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">apply for a certificate (opens in new tab)</a> that confirms this.</p><p class=\"govuk-body\"><a href=\"{CITIZEN_CONTACT_THEM_URL}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">Contact Mr. John Rambo</a> if you need their payment details.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">View your response</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Mae Mr. John Rambo wedi gwneud cais am Ddyfarniad Llys Sirol yn eich erbyn"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae Mr. John Rambo wedi gwrthod eich cynllun ad-dalu ac wedi gofyn i chi lofnodi setliad. " +
                        "Rydych wedi llofnodi’r cytundeb ond mae’r hawlydd yn dweud eich bod wedi torri’r telerau.</p>" +
                        "<p class=\"govuk-body\">Pan fyddwn wedi prosesu’r cais, byddwn yn anfon copi o’r dyfarniad drwy’r post atoch chi.</p><p class=\"govuk-body\">" +
                        "Os byddwch yn talu’r ddyled o fewn mis o ddyddiad y dyfarniad, bydd y CCJ yn cael ei ddileu o’r gofrestr gyhoeddus. Gallwch dalu £15 i " +
                        "<a href=\"{APPLY_FOR_CERTIFICATE}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">wneud cais am dystysgrif " +
                        "(yn agor mewn ffenestr newydd)</a> sy’n cadarnhau hyn.</p><p class=\"govuk-body\">" +
                        "<a href=\"{CITIZEN_CONTACT_THEM_URL}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">Cysylltwch â Mr. John Rambo</a> " +
                        "os oes arnoch angen eu manylion talu.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld eich ymateb</a>" +
                        "</p>")
            );
    }
}
