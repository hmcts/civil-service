package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimSettledDefendantDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimSettledLink;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

public class ClaimSettledAfterCCJRequestedNotificationLinkScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimSettledDefendantDashboardNotificationHandler handler;

    @Test
    void should_create_claim_settled_after_ccj_requested_scenario() throws Exception {

        String caseId = "132872";
        CaseData caseData = CaseDataBuilder.builder().atStateLipClaimSettled().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .caseDataLiP(CaseDataLiP.builder().applicant1SettleClaim(YesOrNo.YES)
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build())
                             .applicant1ClaimSettledLink(ClaimSettledLink.NOTIFICATION)
                             .applicant1ClaimSettledDate(
                                 LocalDate.of(2024, 4, 10)).build()).build();

        handler.handle(callbackParams(caseData));

        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Claim is settled"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Mr. John Rambo confirmed you settled on 10 April 2024. This claim is now settled. " +
                        "If you need proof that the County Court Judgment (CCJ) is paid you can " +
                        "<a href=\"{APPLY_FOR_CERTIFICATE}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">contact us to get a certificate of satisfaction</a>. " +
                        "This costs £15.</p><p class=\"govuk-body\">If you paid within 28 days of the judgment being issued, we'll tell the Registry Trust to remove your CCJ " +
                        "from the register of judgments. The CCJ will not appear in any credit agency searches, though some agencies may not update their records " +
                        "immediately.</p><p class=\"govuk-body\">If you paid after 28 days of the judgment being issued, we'll tell the Registry Trust to mark your CCJ as paid " +
                        "on the register of judgments. Any credit agency that checks the register will see that you've paid, though some may not update records immediately. It " +
                        "will remain on the register for 6 years, but it'll be marked as 'satisfied'. " +
                        "<a href=\"{DOWNLOAD_DEFENDANT_RESPONSE}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">Download your response</a>.</p>"
                ),
                jsonPath("$[0].titleCy").value("Claim is settled"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mr. John Rambo confirmed you settled on 10 Ebrill 2024. This claim is now settled. " +
                        "If you need proof that the County Court Judgment (CCJ) is paid you can " +
                        "<a href=\"{APPLY_FOR_CERTIFICATE}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">contact us to get a certificate of satisfaction</a>. " +
                        "This costs £15.</p><p class=\"govuk-body\">If you paid within 28 days of the judgment being issued, we'll tell the Registry Trust to remove your CCJ " +
                        "from the register of judgments. The CCJ will not appear in any credit agency searches, though some agencies may not update their records " +
                        "immediately.</p><p class=\"govuk-body\">If you paid after 28 days of the judgment being issued, we'll tell the Registry Trust to mark your CCJ as paid " +
                        "on the register of judgments. Any credit agency that checks the register will see that you've paid, though some may not update records immediately. It " +
                        "will remain on the register for 6 years, but it'll be marked as 'satisfied'. " +
                        "<a href=\"{DOWNLOAD_DEFENDANT_RESPONSE}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">Download your response</a>.</p>"
                )
            );
    }
}
