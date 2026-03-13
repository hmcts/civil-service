package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimSettledScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseNotificationHandler handler;

    @Test
    void should_create_scenario_for_claim_settled() throws Exception {

        String caseId = "1234678912";
        LocalDateTime respondent1SettlementDeadline = LocalDateTime.now().plusDays(7);
        CaseDataLiP caseDataLiP = new CaseDataLiP()
            .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                       .setApplicant1SignedSettlementAgreement(YesOrNo.YES));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimantFullDefence().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .caseDataLiP(caseDataLiP)
            .respondent1ClaimResponseTestForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .ccdState(CaseState.CASE_SETTLED)
            .respondToClaim(new RespondToClaim()
                                .setHowMuchWasPaid(BigDecimal.valueOf(300000))
                                .setWhenWasThisAmountPaid(LocalDate.of(2024, 3, 16))
                                )
            .respondent1RespondToSettlementAgreementDeadline(respondent1SettlementDeadline)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The claim is settled"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">You have confirmed that the claim against Mr. Sole Trader was settled on 16 March 2024.</p>"
                            + "<p class=\"govuk-body\">The defendant has 19 days from the date of settlement to notify the court of any objection.</p>"),
                jsonPath("$[0].titleCy").value("Mae’r hawliad wedi’i setlo"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">You have confirmed that the claim against Mr. Sole Trader was settled on 16 March 2024.</p>"
                            + "<p class=\"govuk-body\">The defendant has 19 days from the date of settlement to notify the court of any objection.</p>")
            );
    }
}
