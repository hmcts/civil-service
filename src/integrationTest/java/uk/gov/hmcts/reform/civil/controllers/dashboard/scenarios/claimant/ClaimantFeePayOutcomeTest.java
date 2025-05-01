package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.CreateClaimIssueNotificationsHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantFeePayOutcomeTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CreateClaimIssueNotificationsHandler handler;

    @Test
    void should_create_fee_payment_outcome_scenario() throws Exception {
        String caseId = "7834212";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .hwfFeeType(FeeType.CLAIMISSUED)
            .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder()
                                          .hwfFullRemissionGrantedForClaimIssue(YesOrNo.NO).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The claim fee has been paid"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The claim fee has been paid in full.</p>"),
                jsonPath("$[0].titleCy").value("Mae ffi’r hawliad wedi cael ei thalu"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae ffi’r hawliad wedi cael ei thalu’n llawn.</p>")
            );
    }
}
