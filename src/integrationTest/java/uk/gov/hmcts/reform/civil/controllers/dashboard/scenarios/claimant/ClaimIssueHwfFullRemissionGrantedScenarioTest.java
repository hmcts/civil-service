package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.HwFDashboardNotificationsHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimIssueHwfFullRemissionGrantedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    HwFDashboardNotificationsHandler hwFDashboardNotificationsHandler;
    public static final String caseId  = "000MC001";

    @Test
    void should_create_claim_issue_hwf_full_remission_scenario() throws Exception {

        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(455000)).build())
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().hwfCaseEvent(CaseEvent.FULL_REMISSION_HWF).build())
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        //When
        hwFDashboardNotificationsHandler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "Your help with fees application has been reviewed"),
            jsonPath("$[0].descriptionEn").value(
                "The full claim fee of £455 will be covered. You do not need to make a payment."),
            jsonPath("$[0].titleCy").value(
                "Your help with fees application has been reviewed"),
            jsonPath("$[0].descriptionCy").value(
                "The full claim fee of £455 will be covered. You do not need to make a payment.")
        );
    }
}
