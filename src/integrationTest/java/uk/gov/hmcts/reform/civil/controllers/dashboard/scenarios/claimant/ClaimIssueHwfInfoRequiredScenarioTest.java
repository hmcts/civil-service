package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.HwFDashboardNotificationsHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF;

public class ClaimIssueHwfInfoRequiredScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HwFDashboardNotificationsHandler handler;

    @Test
    void should_create_claim_issue_hwf_info_required_scenario() throws Exception {

        String caseId = "12346780";

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .hwfFeeType(FeeType.CLAIMISSUED)
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().hwfCaseEvent(MORE_INFORMATION_HWF).build())
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value("Your help with fees application needs more information"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">We need more information on your application for help with the claim fee. You've been sent an email with further details." +
                    " If you've already read the email and taken action, you can disregard this message. You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>"),
            jsonPath("$[0].titleCy").value("Mae angen i chi ddarparu mwy o wybodaeth am eich cais am help i dalu ffioedd"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Mae arnom angen mwy o wybodaeth am eich cais am help i dalu ffi’r hawliad. Anfonwyd e-bost atoch gyda mwy o fanylion." +
                    " Os ydych eisoes wedi darllen yr e-bost ac wedi gweithredu, gallwch anwybyddu'r neges hon. " +
                    "Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephoneWelshSpeaker}.</p>")

        );
    }

}
