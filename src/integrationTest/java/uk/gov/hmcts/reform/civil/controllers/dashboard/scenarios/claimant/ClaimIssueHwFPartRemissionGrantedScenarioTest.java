package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.HwFDashboardNotificationsHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;

public class ClaimIssueHwFPartRemissionGrantedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HwFDashboardNotificationsHandler handler;

    @Test
    @DirtiesContext
    void should_create_claim_issue_hwf_part_remission_scenario() throws Exception {

        String caseId = "12346783";

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .applicant1Represented(YesOrNo.NO)
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                       .hwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED)
                                       .remissionAmount(BigDecimal.valueOf(100000))
                                       .outstandingFeeInPounds(BigDecimal.valueOf(25))
                                       .build())
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "Your help with fees application has been reviewed"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You'll get help with the claim fee. £1000 will be covered by fee remission. You must still pay the remaining fee of £25. " +
                    "You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>"),
            jsonPath("$[0].titleCy").value(
                "Mae eich cais am help i dalu ffioedd wedi cael ei adolygu"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Byddwch yn cael help gyda ffi’r hawliad. Bydd y swm o £1000 yn cael ei ddileu. " +
                    "Bydd rhaid i chi dal dalu’r ffi sy’n weddill o £25. Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephoneWelshSpeaker}.</p>")

        );
    }
}
