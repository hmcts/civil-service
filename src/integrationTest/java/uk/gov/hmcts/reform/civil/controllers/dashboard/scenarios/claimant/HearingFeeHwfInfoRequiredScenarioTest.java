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

public class HearingFeeHwfInfoRequiredScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HwFDashboardNotificationsHandler hwFDashboardNotificationsHandler;

    @BeforeEach
    public void before() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
    }

    @Test
    @DirtiesContext
    void should_create_hearing_fee_hwf_info_required_scenario() throws Exception {
        String caseId = "12345";

        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheckLiP(false).build()
            .toBuilder()
            .legacyCaseReference("reference")
            .applicant1Represented(YesOrNo.NO)
            .ccdCaseReference(Long.valueOf(caseId))
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(45500)).build())
            .hearingHwfDetails(HelpWithFeesDetails.builder().hwfCaseEvent(CaseEvent.MORE_INFORMATION_HWF).build())
            .hwfFeeType(FeeType.HEARING)
            .build();

        //When
        hwFDashboardNotificationsHandler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "Your help with fees application needs more information"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">We need more information on your application for help with the hearing fee." +
                    " You've been sent an email with further details. If you've already read the email and taken action, " +
                    "you can disregard this message. You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>"),
            jsonPath("$[0].titleCy").value(
                "Mae angen i chi nodi mwy o wybodaeth ar eich cais am help i dalu ffioedd"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Mae arnom angen mwy o wybodaeth am eich cais am help i dalu ffi'r "
                    + "gwrandawiad. Anfonwyd e-bost atoch gyda mwy o fanylion. Os ydych eisoes wedi darllen yr "
                    + "e-bost ac wedi gweithredu, gallwch anwybyddu'r neges hon. Gallwch dalu dros y ffôn drwy "
                    + "ffonio {civilMoneyClaimsTelephone}.</p>")
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
