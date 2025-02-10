package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HearingFeeHwfInvalidReferenceScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HwFDashboardNotificationsHandler hwFDashboardNotificationsHandler;

    @BeforeEach
    public void before() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
    }

    @Test
    @DirtiesContext
    void should_create_hearing_fee_hwf_invalid_reference_scenario() throws Exception {
        String caseId = "12345";

        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheckLiP(false).build()
            .toBuilder()
            .legacyCaseReference("reference")
            .applicant1Represented(YesOrNo.NO)
            .ccdCaseReference(Long.valueOf(caseId))
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(45500)).build())
            .hearingHwfDetails(HelpWithFeesDetails.builder().hwfCaseEvent(CaseEvent.INVALID_HWF_REFERENCE).build())
            .hwfFeeType(FeeType.HEARING)
            .build();

        //When
        hwFDashboardNotificationsHandler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "You've provided an invalid help with fees reference number"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You've applied for help with the hearing fee, but the reference number is invalid. " +
                    "You've been sent an email with instructions on what to do next. If you've already read the email and taken action, " +
                    "you can disregard this message. You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>"),
            jsonPath("$[0].titleCy").value(
                "Gwnaethoch ddarparu cyfeirnod help i dalu ffioedd annilys"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Rydych wedi gwneud cais am help i dalu ffi'r gwrandawiad, ond "
                    + "mae'r cyfeirnod yn annilys. Anfonwyd e-bost atoch gyda chyfarwyddiadau ar beth i'w wneud "
                    + "nesaf. Os ydych eisoes wedi darllen yr e-bost ac wedi gweithredu, gallwch anwybyddu'r neges "
                    + "hon. Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}.</p>")
        );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Pay the hearing fee</a>"),
                jsonPath("$[0].currentStatusEn").value("In progress")
            );
    }
}
