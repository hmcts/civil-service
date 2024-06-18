package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourtFavoursClaimantSettlementAgreementScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_scenario_for_court_favours_defendant_sign_settlement_agreement() throws Exception {

        String caseId = "12348991011";
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmission().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1SignedSettlementAgreement(YesOrNo.YES)
                                                        .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                                                        .build())
                             .build())
            .specRespondent1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .respondent1RespondToSettlementAgreementDeadline(LocalDateTime.of(2024, 3, 16, 0, 0, 0))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Settlement agreement"),
                jsonPath("$[0].titleCy").value("Cytundeb setlo"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">Mr. John Rambo has rejected your offer and asked " +
                            "you to sign a settlement agreement.</p>" +
                            "<p class=\"govuk-body\">Mr. John Rambo has proposed a new repayment plan and the court has agreed with it, " +
                            "based on the financial details you provided.</p>" +
                            "<p class=\"govuk-body\">You must respond by 16 March 2024. If you do not respond by then, " +
                            "or reject the agreement, they can request a County Court Judgment (CCJ).</p>" +
                            "<p class=\"govuk-body\">You can <a href=\"{VIEW_REPAYMENT_PLAN}\"  rel=\"noopener noreferrer\" " +
                            "class=\"govuk-link\">view the repayment plan</a> or " +
                            "<a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">view your response</a>.</p>"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Mae Mr. John Rambo wedi gwrthod eich cynnig ac wedi gofyn " +
                            "i chi lofnodi cytundeb setlo.</p>" +
                            "<p class=\"govuk-body\">Mae Mr. John Rambo wedi cynnig cynllun ad-dalu newydd, " +
                            "ac mae’r llys wedi cytuno iddo, yn seiliedig ar y manylion ariannol a ddarparwyd gennych.</p>" +
                            "<p class=\"govuk-body\">Mae’n rhaid i chi ymateb erbyn 16 Mawrth 2024. Os na fyddwch wedi " +
                            "ymateb erbyn hynny, neu os byddwch yn gwrthod y cytundeb, gallant wneud cais am Ddyfarniad Llys Sifil (CCJ).</p>" +
                            "<p class=\"govuk-body\">Gallwch <a href=\"{VIEW_REPAYMENT_PLAN}\"  rel=\"noopener noreferrer\" " +
                            "class=\"govuk-link\">weld y cynllun ad-dalu</a> neu " +
                            "<a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">weld eich ymateb</a>.</p>")
            );
    }
}
