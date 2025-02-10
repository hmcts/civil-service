package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantCCJResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantAcceptsRepaymentPlanCCJDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantCCJResponseDefendantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_accepts_repayment_plan_ccj_for_defendant() throws Exception {

        String caseId = "12345147677";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .caseDataLiP(CaseDataLiP.builder().applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                                         .applicant1ChoosesHowToProceed(
                                                                             ChooseHowToProceed.REQUEST_A_CCJ).build())
                             .build())
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mr. John Rambo has requested a County Court Judgment against you"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Mr. John Rambo accepted your repayment plan. When we've processed the request, " +
                        "we'll post a copy of the judgment to you.</p> <p class=\"govuk-body\">If you pay the debt within one month of the date of judgment, " +
                        "the County Court Judgment (CCJ) is removed from the public register. You can pay £15 to " +
                        "<a href=\"{APPLY_FOR_CERTIFICATE}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">apply for a certificate (opens in new tab)</a> " +
                        "that confirms this.</p> <p class=\"govuk-body\"><a href=\"{CITIZEN_CONTACT_THEM_URL}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">" +
                        "Contact Mr. John Rambo</a> if you need their payment details. <br> <a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">View your response</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Mae Mr. John Rambo wedi gwneud cais am Ddyfarniad Llys Sirol yn eich erbyn"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae Mr. John Rambo wedi derbyn eich cynllun ad-dalu. Pan fyddwn wedi prosesu’r cais, " +
                        "byddwn yn anfon copi o’r dyfarniad drwy’r post atoch chi.</p><p class=\"govuk-body\">Os byddwch yn talu’r ddyled o fewn " +
                        "mis o ddyddiad y dyfarniad, bydd y Dyfarniad Llys Sirol (CCJ) yn cael ei ddileu o’r gofrestr gyhoeddus. Gallwch dalu £15 i " +
                        "<a href=\"{APPLY_FOR_CERTIFICATE}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">wneud cais am dystysgrif" +
                        " (yn agor mewn tab newydd)</a> sy’n cadarnhau hyn.</p> " +
                        "<p class=\"govuk-body\"><a href=\"{CITIZEN_CONTACT_THEM_URL}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">" +
                        "Cysylltwch â Mr. John Rambo</a> os oes arnoch angen eu manylion talu. <br>" +
                        " <a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld eich ymateb</a></p>")
            );
    }
}
