package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.HwFDashboardNotificationsHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;

public class NoRemissionHwFScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HwFDashboardNotificationsHandler handler;

    @Test
    void should_create_no_remission_scenario() throws Exception {

        String caseId = "12346782";
        String claimFee = "100";

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .hwfFeeType(FeeType.CLAIMISSUED)
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).build())
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().hwfCaseEvent(NO_REMISSION_HWF).build())
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn")
                    .value("Your help with fees application has been rejected"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">We've rejected your application for help with the claim fee. See the email for "
                        + "further details.</p><p class=\"govuk-body\">You must pay the full fee of £"
                        + claimFee + ". You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>"),
                jsonPath("$[0].titleCy")
                    .value("Mae eich cais am help i dalu ffioedd wedi cael ei wrthod"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydym wedi gwrthod eich cais am help i dalu ffi’r hawliad. Gweler yr e-bost am ragor o fanylion.</p>" +
                        "<p class=\"govuk-body\">Rhaid i chi dalu’r ffi lawn o £"
                        + claimFee + ". Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephoneWelshSpeaker}.</p>")
            );

    }

}
