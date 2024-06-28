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
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantAcceptDefendantSettlementAgreementScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_scenario_for_claimant_accept_defendant_plan_settlement_agreement() throws Exception {

        String caseId = "90123456785";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1FullDefenceConfirmAmountPaidSpec(YesOrNo.NO)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .respondent1RespondToSettlementAgreementDeadline(LocalDateTime.of(2024, 3, 16, 0, 0, 0))
            .caseDataLiP(
                CaseDataLiP.builder()
                    .respondentSignSettlementAgreement(YesOrNo.NO)
                    .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                               .applicant1SignedSettlementAgreement(YesOrNo.YES).build())
                    .build()
            )
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
                        "<p class=\"govuk-body\">Mr. John Rambo has accepted your offer and asked you to sign a settlement agreement. " +
                            "You must respond by 16 March 2024.</p><p class=\"govuk-body\">" +
                            "If you do not respond by then, or reject the agreement, they can request a County Court Judgment (CCJ).</p><p class=\"govuk-body\">You can " +
                            "<a href=\"{VIEW_REPAYMENT_PLAN}\" rel=\"noopener noreferrer\" class=\"govuk-link\">view the repayment plan</a> or " +
                            "<a href=\"{VIEW_RESPONSE_TO_CLAIM}\" rel=\"noopener noreferrer\" class=\"govuk-link\">view your response</a>.</p>"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Mae Mr. John Rambo wedi derbyn eich cynnig ac wedi gofyn i chi lofnodi cytundeb setlo. " +
                            "Mae’n rhaid i chi ymateb erbyn 16 Mawrth 2024.</p><p class=\"govuk-body\">Os na fyddwch wedi ymateb erbyn hynny, neu os " +
                            "byddwch yn gwrthod y cytundeb, gallant wneud cais am Ddyfarniad Llys Sifil (CCJ).</p><p class=\"govuk-body\">Gallwch " +
                            "<a href=\"{VIEW_REPAYMENT_PLAN}\" rel=\"noopener noreferrer\" class=\"govuk-link\">weld y cynllun ad-dalu</a>" +
                            " neu <a href=\"{VIEW_RESPONSE_TO_CLAIM}\" rel=\"noopener noreferrer\" class=\"govuk-link\">weld eich ymateb</a>.</p>")
            );
    }
}
