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
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                       .applicant1SignedSettlementAgreement(YesOrNo.YES).build()).build();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimantFullDefence().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .caseDataLiP(caseDataLiP)
            .respondent1ClaimResponseTestForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .ccdState(CaseState.CASE_SETTLED)
            .respondToClaim(RespondToClaim.builder()
                                .howMuchWasPaid(BigDecimal.valueOf(300000))
                                .whenWasThisAmountPaid(LocalDate.of(2024, 3, 16))
                                .build())
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
                        "<p class=\"govuk-body\">You have confirmed that Mr. Sole Trader paid £3000 on 16 March 2024.</p>")
            );
    }
}
