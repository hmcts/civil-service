package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimSettledDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimSettledLink;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

public class ClaimSettledAfterCCJRequestedNotificationLinkScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimSettledDashboardNotificationHandler handler;

    @Test
    void should_create_claim_settled_after_ccj_requested_scenario() throws Exception {

        String caseId = "132871";
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

        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Claim is settled"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Mr. Sole Trader paid you on 10 April 2024.</p>" +
                        "<p class=\"govuk-body\">If the defendant paid you within 28 days of the judgment being issued then the defendant's County Court Judgment " +
                        "will be cancelled.</p><p class=\"govuk-body\">If the defendant paid you after this, then the judgment will be marked as paid.</p>"
                ),
                jsonPath("$[0].titleCy").value("Claim is settled"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mr. Sole Trader paid you on 10 April 2024.</p>" +
                        "<p class=\"govuk-body\">If the defendant paid you within 28 days of the judgment being issued then the defendant's County Court Judgment " +
                        "will be cancelled.</p><p class=\"govuk-body\">If the defendant paid you after this, then the judgment will be marked as paid.</p>"
                )
            );
    }
}
