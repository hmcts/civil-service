package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimSettledCourtDecisionInFavorOfDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_scenario_for_claim_settle() throws Exception {

        String caseId = "1234899109";
        LocalDateTime responseDeadline = LocalDateTime.now();
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .claimantCourtDecision(RepaymentDecisionType
                                                                                   .IN_FAVOUR_OF_DEFENDANT).build())
                             .build())
            .specRespondent1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .respondent1RespondToSettlementAgreementDeadline(responseDeadline)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Settlement agreement"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">Mr. John Rambo has rejected your offer and asked you to "
                               + "sign a settlement agreement."
                               + "</p><p class=\"govuk-body\">"
                               + "Mr. John Rambo proposed a repayment plan, and the court "
                               + "then responded with an alternative plan that was accepted."
                               + "</p><p class=\"govuk-body\">"
                               + " You must respond by " + DateUtils.formatDate(responseDeadline) + ". If you do not respond by then, "
                               + "or reject the agreement, they can request a County Court Judgment (CCJ).</p><p"
                               +
                               " class=\"govuk-body\">You can <a href=\"{VIEW_REPAYMENT_PLAN}\"  rel=\"noopener noreferrer\" "
                               + "class=\"govuk-link\">view the repayment plan</a> or <a "
                               + "href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener "
                               + "noreferrer\" class=\"govuk-link\">view your response</a>.</p>"),
                jsonPath("$[0].titleCy").value("Cytundeb setlo"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">Mae Mr. John Rambo wedi gwrthod eich cynnig ac wedi gofyn i chi lofnodi cytundeb setlo.</p>" +
                               "<p class=\"govuk-body\">Mi wnaeth Mr. John Rambo gynnig cynllun ad-dalu newydd, ac yna mi wnaeth " +
                               "y llys ymateb gyda chynllun arall a gafodd ei dderbyn.</p>" +
                               "<p class=\"govuk-body\"> Mae’n rhaid i chi ymateb erbyn " + DateUtils.formatDateInWelsh(responseDeadline.toLocalDate()) +
                               ". Os na fyddwch wedi ymateb erbyn hynny, neu os byddwch yn gwrthod y cytundeb, gallant wneud cais am Ddyfarniad Llys Sifil (CCJ).</p>" +
                               "<p class=\"govuk-body\">Gallwch <a href=\"{VIEW_REPAYMENT_PLAN}\"  rel=\"noopener noreferrer\" " +
                               "class=\"govuk-link\">weld y cynllun ad-dalu</a> neu <a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  " +
                               "rel=\"noopener noreferrer\" class=\"govuk-link\">weld eich ymateb</a>.</p>")
            );
    }
}
