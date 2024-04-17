package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.HwFDashboardNotificationsHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HearingFeeHwfPartialRemissionGrantedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HwFDashboardNotificationsHandler hwFDashboardNotificationsHandler;

    @Test
    void should_create_hearing_fee_hwf_partial_remission_scenario() throws Exception {
        String caseId = "12345";

        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheckLiP(false).build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(45500)).build())
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.PARTIAL_REMISSION_HWF_GRANTED)
                                   .remissionAmount(new BigDecimal(10000))
                                   .outstandingFeeInPounds(new BigDecimal(355))
                                   .build())
            .hwfFeeType(FeeType.HEARING)
            .build();

        //When
        hwFDashboardNotificationsHandler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "Your help with fees application has been reviewed"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You'll get help with the hearing fee. £100 will be covered by fee remission. You must still pay the remaining fee of £355. You can pay by phone by calling {civilMoneyClaimsTelephone}. If you do not pay, your claim will be struck out.</p>"),
            jsonPath("$[0].titleCy").value(
                "Your help with fees application has been reviewed"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">You'll get help with the hearing fee. £100 will be covered by fee remission. You must still pay the remaining fee of £355. You can pay by phone by calling {civilMoneyClaimsTelephone}. If you do not pay, your claim will be struck out.</p>")
        );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Pay the hearing fee</a>"),
                jsonPath("$[0].currentStatusEn").value("Action needed")
            );
    }
}
