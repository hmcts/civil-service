package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.HearingFeePaidClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

public class HearingFeePaidClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HearingFeePaidClaimantNotificationHandler handler;

    @Test
    void should_create_fee_payment_outcome_scenario_when_payment_via_phone() throws Exception {
        String caseId = "7834212";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().applicant1Represented((YesOrNo.NO)).build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .hwfFeeType(FeeType.HEARING)
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                          .setHwfFullRemissionGrantedForHearingFee(YesOrNo.NO))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The hearing fee has been paid"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The hearing fee has been paid in full.</p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId),
                jsonPath("$[0].taskNameEn").value(
                    "<a>Pay the hearing fee</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.DONE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a>Talu ffi'r gwrandawiad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.DONE.getWelshName())
            );
    }

    @Test
    void should_create_fee_payment_outcome_scenario_when_payment_via_gov_pay() throws Exception {
        String caseId = "7834212";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().applicant1Represented((YesOrNo.NO)).build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .build();
        caseData = caseData.toBuilder().hearingFeePaymentDetails(new PaymentDetails().setStatus(SUCCESS).setReference("REFERENCE")).build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The hearing fee has been paid"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The hearing fee has been paid in full.</p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId),
                jsonPath("$[0].taskNameEn").value(
                    "<a>Pay the hearing fee</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.DONE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a>Talu ffi'r gwrandawiad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.DONE.getWelshName())
            );
    }
}
