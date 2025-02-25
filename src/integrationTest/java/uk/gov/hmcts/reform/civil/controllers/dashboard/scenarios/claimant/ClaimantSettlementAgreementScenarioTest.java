package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantSettlementAgreementScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseNotificationHandler handler;

    @Test
    void should_create_claimant_settlement_agreement_scenario() throws Exception {

        LocalDateTime respondent1SettlementDeadline = LocalDateTime.now();

        String caseId = "12346789";
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                       .applicant1SignedSettlementAgreement(YesOrNo.YES).build()).build();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimantFullDefence().build()
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .caseDataLiP(caseDataLiP)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .respondent1RespondToSettlementAgreementDeadline(respondent1SettlementDeadline)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Settlement agreement"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You have accepted the Mr. Sole Trader offer and asked " +
                        "them to sign a settlement agreement.</p><p class=\"govuk-body\">The defendant " +
                        "must respond by " + DateUtils.formatDate(respondent1SettlementDeadline) +
                        ".</p><p class=\"govuk-body\">If they do not respond by then, or reject the agreement, " +
                        "you can request a County Court Judgment(CCJ).</p>"),
                jsonPath("$[0].titleCy").value("Cytundeb setlo"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydych wedi derbyn cynnig Mr. Sole Trader ac wedi gofyn " +
                        "iddynt lofnodi cytundeb setlo.</p><p class=\"govuk-body\">Mae’n rhaid i’r diffynnydd ymateb " +
                        "erbyn " +
                        DateUtils.formatDateInWelsh(respondent1SettlementDeadline.toLocalDate(), false) +
                        ".</p><p class=\"govuk-body\">Os na fyddant wedi ymateb erbyn hynny, neu os byddant yn gwrthod " +
                        "y cytundeb, gallwch wneud cais am Ddyfarniad Llys Sifil (CCJ).</p>")
            );
    }
}
