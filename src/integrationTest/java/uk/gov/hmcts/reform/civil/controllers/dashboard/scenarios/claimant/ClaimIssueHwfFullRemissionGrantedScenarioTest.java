package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
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

public class ClaimIssueHwfFullRemissionGrantedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HwFDashboardNotificationsHandler hwFDashboardNotificationsHandler;

    @Test
    void should_create_claim_issue_hwf_full_remission_scenario() throws Exception {
        String caseId = "12345";

        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(45500)).build())
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().hwfCaseEvent(CaseEvent.FULL_REMISSION_HWF).build())
            .hwfFeeType(FeeType.CLAIMISSUED)
            .applicant1Represented(YesOrNo.NO)
            .build();

        //When
        hwFDashboardNotificationsHandler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "Your help with fees application has been approved"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">The full claim fee of £455 will be covered by fee remission. You do not need to make a payment.</p>"),
            jsonPath("$[0].titleCy").value(
                "Mae eich cais am help i dalu ffioedd wedi cael ei gymeradwyo"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Bydd ffi lawn yr hawliad o £455 yn cael ei ddileu. Nid oes angen i chi wneud taliad.</p>")
        );
    }
}
