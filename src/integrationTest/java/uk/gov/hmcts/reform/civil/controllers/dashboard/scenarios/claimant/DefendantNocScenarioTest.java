package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantNocDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class DefendantNocScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantNocDashboardNotificationHandler handler;

    @Test
    void shouldCreateNotificationForDefendantNoc() throws Exception {
        String caseId = "323491";
        CaseData caseData = buildCaseData(caseId);

        handler.handle(callbackParams(caseData));

        verifyNotification(caseId);
        verifyTaskItems(caseId);
    }

    @Test
    void shouldUpdateTrialArrangementsTaskList() throws Exception {
        String caseId = "323491";
        CaseData caseData = buildCaseDataWithTrialArrangements(caseId);

        handler.handle(callbackParams(caseData));

        verifyTaskItemsWithTrialArrangements(caseId);
    }

    @Test
    void shouldUpdateHearingFeeTaskList() throws Exception {
        String caseId = "323491";
        CaseData caseData = buildCaseDataWithHearingFee(caseId);

        handler.handle(callbackParams(caseData));

        verifyTaskItemsWithHearingFee(caseId);
    }

    private CaseData buildCaseData(String caseId) {
        PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.SUCCESS);
        return CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .hearingFeePaymentDetails(paymentDetails)
            .build();
    }

    private CaseData buildCaseDataWithTrialArrangements(String caseId) {
        PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.SUCCESS);
        return CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .trialReadyApplicant(null)
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .orderType(OrderType.DECIDE_DAMAGES)
            .hearingFeePaymentDetails(paymentDetails)
            .build();
    }

    private CaseData buildCaseDataWithHearingFee(String caseId) {
        return CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .trialReadyApplicant(null)
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .orderType(OrderType.DECIDE_DAMAGES)
            .hearingFeePaymentDetails(null)
            .build();
    }

    private void verifyNotification(String caseId) throws Exception {
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Your online account will no longer be updated"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Your online account will no longer be updated. If there are any further updates to your case these will be by post.</p>"),
                jsonPath("$[0].titleCy").value("Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru mwyach"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru mwyach. Os oes unrhyw ddiweddariadau pellach i’ch achos, bydd y rhain yn " +
                        "cael eu hanfon atoch drwy'r post.</p>")
            );
    }

    private void verifyTaskItems(String caseId) throws Exception {
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[0].taskNameCy").value("<a>Llwytho dogfennau'r gwrandawiad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[1].reference").value(caseId.toString()),
                jsonPath("$[1].taskNameEn").value("<a>Contact the court to request a change to my case</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[1].taskNameCy").value("<a>Cysylltu â’r llys i wneud cais am newid i fy achos</a>"),
                jsonPath("$[1].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[2].taskNameEn").value("<a>View applications</a>"),
                jsonPath("$[2].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[2].taskNameCy").value("<a>Gweld y cais i gyd</a>"),
                jsonPath("$[2].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName())
            );
    }

    private void verifyTaskItemsWithTrialArrangements(String caseId) throws Exception {
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[0].taskNameCy").value("<a>Llwytho dogfennau'r gwrandawiad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[1].reference").value(caseId.toString()),
                jsonPath("$[1].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[1].taskNameCy").value("<a>Ychwanegu trefniadau'r treial</a>"),
                jsonPath("$[1].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[2].reference").value(caseId.toString()),
                jsonPath("$[2].taskNameEn").value("<a>Contact the court to request a change to my case</a>"),
                jsonPath("$[2].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[2].taskNameCy").value("<a>Cysylltu â’r llys i wneud cais am newid i fy achos</a>"),
                jsonPath("$[2].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[3].taskNameEn").value("<a>View applications</a>"),
                jsonPath("$[3].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[3].taskNameCy").value("<a>Gweld y cais i gyd</a>"),
                jsonPath("$[3].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName())
            );
    }

    private void verifyTaskItemsWithHearingFee(String caseId) throws Exception {
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Pay the hearing fee</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[0].taskNameCy").value("<a>Talu ffi'r gwrandawiad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[1].reference").value(caseId.toString()),
                jsonPath("$[1].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[1].taskNameCy").value("<a>Llwytho dogfennau'r gwrandawiad</a>"),
                jsonPath("$[1].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[2].reference").value(caseId.toString()),
                jsonPath("$[2].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[2].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[2].taskNameCy").value("<a>Ychwanegu trefniadau'r treial</a>"),
                jsonPath("$[2].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[3].reference").value(caseId.toString()),
                jsonPath("$[3].taskNameEn").value("<a>Contact the court to request a change to my case</a>"),
                jsonPath("$[3].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[3].taskNameCy").value("<a>Cysylltu â’r llys i wneud cais am newid i fy achos</a>"),
                jsonPath("$[3].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[4].taskNameEn").value("<a>View applications</a>"),
                jsonPath("$[4].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[4].taskNameCy").value("<a>Gweld y cais i gyd</a>"),
                jsonPath("$[4].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName())

            );
    }
}
