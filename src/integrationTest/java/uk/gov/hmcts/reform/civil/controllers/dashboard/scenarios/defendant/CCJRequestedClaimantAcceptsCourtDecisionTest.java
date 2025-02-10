package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantCCJResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

public class CCJRequestedClaimantAcceptsCourtDecisionTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantCCJResponseDefendantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_claimant_accepts_court_decision_scenario() throws Exception {

        String caseId = "13074";
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .caseDataLiP(CaseDataLiP.builder()
                .applicant1LiPResponse(ClaimantLiPResponse.builder()
                    .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT)
                    .claimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.ACCEPT_REPAYMENT_DATE)
                    .build())
                .build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mr. John Rambo has requested a County Court Judgment against you"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Mr. John Rambo rejected your <a href={CCJ_REPAYMENT_PLAN_DEFENDANT_URL} class=\"govuk-link\">repayment plan</a> and proposed a new plan. The court then responded with an alternative plan that was accepted in favour of you.</p>"
                        + "<p class=\"govuk-body\">When we've processed the request, we'll post a copy of the judgment to you.</p>"
                        + "<p class=\"govuk-body\">If you pay the debt within one month of the date of judgment, the County Court Judgment (CCJ) is removed from the public register. You can pay £15 to <a href=\"{APPLY_FOR_CERTIFICATE}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">apply for a certificate (opens in new tab)</a> that confirms this.</p>"
                        + "<p class=\"govuk-body\"><a href=\"{CITIZEN_CONTACT_THEM_URL}\" class=\"govuk-link\">Contact Mr. John Rambo</a> if you need their payment details.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">View your response</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Mae Mr. John Rambo wedi gwneud cais am Ddyfarniad Llys Sirol yn eich erbyn"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mi wnaeth Mr. John Rambo wrthod eich <a href={CCJ_REPAYMENT_PLAN_DEFENDANT_URL} class=\"govuk-link\">cynllun ad-dalu</a> a chynnig cynllun newydd. Yna mi wnaeth y llys ymateb gyda chynllun arall a gafodd ei dderbyn o’ch plaid chi.</p>"
                        + "<p class=\"govuk-body\">Pan fyddwn wedi prosesu’r cais, byddwn yn anfon copi o’r dyfarniad drwy’r post atoch chi.</p>"
                        + "<p class=\"govuk-body\">Os byddwch yn talu’r ddyled o fewn mis o ddyddiad y dyfarniad, bydd y Dyfarniad Llys Sirol (CCJ) yn cael ei ddileu o’r gofrestr gyhoeddus. Gallwch dalu £15 i <a href=\"{APPLY_FOR_CERTIFICATE}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">wneud cais am dystysgrif (yn agor mewn tab newydd)</a> sy’n cadarnhau hyn.</p>"
                        + "<p class=\"govuk-body\"><a href=\"{CITIZEN_CONTACT_THEM_URL}\" class=\"govuk-link\">Cysylltwch â Mr. John Rambo</a> os oes arnoch angen eu manylion talu.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">Gweld eich ymateb</a></p>"
                )
            );
    }
}
