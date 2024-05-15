package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
                    .value("<p class=\"govuk-body\">The claimant has rejected your plan and asked you to "
                               + "sign a settlement agreement."
                               + "</p><p class=\"govuk-body\">"
                               + "The claimant proposed a repayment plan, and the court "
                               + "then responded with an alternative plan that was accepted."
                               + "</p><p class=\"govuk-body\">"
                               + " You must respond by " + DateUtils.formatDate(responseDeadline) + ". If you do not respond by then, "
                               + "or reject the agreement, they can request a County Court Judgment.</p><p"
                               + " class=\"govuk-body\"><a href=\"{VIEW_REPAYMENT_PLAN}\"  rel=\"noopener noreferrer\" "
                               + "class=\"govuk-link\">View the repayment plan</a><br><a "
                               + "href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener "
                               + "noreferrer\" class=\"govuk-link\">View your response</a></p>")
            );
    }
}
