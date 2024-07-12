package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HearingFeeHwfRejectedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HwFDashboardNotificationsHandler hwFDashboardNotificationsHandler;

    @BeforeEach
    public void before() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
    }

    @Test
    void should_create_hearing_fee_hwf_rejected_scenario() throws Exception {
        String caseId = "12345";

        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheckLiP(false).build()
            .toBuilder()
            .legacyCaseReference("reference")
            .applicant1Represented(YesOrNo.NO)
            .ccdCaseReference(Long.valueOf(caseId))
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(45500)).build())
            .hearingHwfDetails(HelpWithFeesDetails.builder().hwfCaseEvent(CaseEvent.NO_REMISSION_HWF).build())
            .hearingDueDate(LocalDate.of(2024, 4, 4))
            .hwfFeeType(FeeType.HEARING)
            .build();

        //When
        hwFDashboardNotificationsHandler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "Your help with fees application has been rejected"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">We've rejected your application for help with the hearing fee. See the email for further details. You must <a href={PAY_HEARING_FEE} class=\"govuk-link\">pay the full fee</a> of £455 by 4 April 2024. You can pay by phone by calling {civilMoneyClaimsTelephone}. If you do not pay your claim will be struck out.</p>"),
            jsonPath("$[0].titleCy").value(
                "Mae eich cais am help i dalu ffioedd wedi cael ei wrthod"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Rydym wedi gwrthod eich cais am help i dalu ffi'r gwrandawiad. Gweler yr e-bost am ragor o fanylion. Rhaid i chi <a href={PAY_HEARING_FEE} class=\"govuk-link\">dalu'r ffi lawn</a> o £455 erbyn "
                    + DateUtils.formatDateInWelsh(LocalDate.of(2024, 4, 4))
                    + ". Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}. Os na fyddwch yn talu, bydd eich hawliad yn cael ei ddileu.</p>")
        );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={PAY_HEARING_FEE} class=\"govuk-link\">Pay the hearing fee</a>"),
                jsonPath("$[0].currentStatusEn").value("Action needed")
            );
    }
}
