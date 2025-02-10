package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.HearingFeeUnpaidClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class HearingFeeUnpaidScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HearingFeeUnpaidClaimantNotificationHandler handler;

    @BeforeEach
    public void before() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
    }

    @Test
    @DirtiesContext
    void should_create_hearing_fee_unpaid_scenario() throws Exception {

        String caseId = "14323241";
        LocalDate hearingDueDate = LocalDate.now().minusDays(1);
        CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDueUnpaid().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(NO)
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .orderType(OrderType.DECIDE_DAMAGES)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The claim has been struck out"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">This is because the hearing fee was not paid by "
                        + DateUtils.formatDate(hearingDueDate)
                        + " as stated in the <a href=\"{VIEW_HEARING_NOTICE}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">hearing notice</a>.</p>"),
                jsonPath("$[0].titleCy").value("Mae'r hawliad wedi cael ei ddileu"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Y rheswm am hyn yw na thalwyd ffi'r gwrandawiad erbyn "
                        + DateUtils.formatDateInWelsh(hearingDueDate)
                        + " fel y nodir yn yr <a href=\"{VIEW_HEARING_NOTICE}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">hysbysiad o wrandawiad</a>.</p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Pay the hearing fee</a>"),
                jsonPath("$[0].currentStatusEn").value("Inactive"),
                jsonPath("$[1].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[1].currentStatusEn").value("Inactive"),
                jsonPath("$[2].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[2].currentStatusEn").value("Inactive")

            );
    }

    @Test
    @DirtiesContext
    void should_create_hearing_fee_unpaid_scenario_without_trial_arrangements_when_small_claims() throws Exception {

        String caseId = "14323240";
        LocalDate hearingDueDate = LocalDate.now().minusDays(1);
        CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDueUnpaid().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The claim has been struck out"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">This is because the hearing fee was not paid by "
                        + DateUtils.formatDate(hearingDueDate)
                        + " as stated in the <a href=\"{VIEW_HEARING_NOTICE}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">hearing notice</a>.</p>"),
                jsonPath("$[0].titleCy").value("Mae'r hawliad wedi cael ei ddileu"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Y rheswm am hyn yw na thalwyd ffi'r gwrandawiad erbyn "
                        + DateUtils.formatDateInWelsh(hearingDueDate)
                        + " fel y nodir yn yr <a href=\"{VIEW_HEARING_NOTICE}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">hysbysiad o wrandawiad</a>.</p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Pay the hearing fee</a>"),
                jsonPath("$[0].currentStatusEn").value("Inactive"),
                jsonPath("$[1].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[1].currentStatusEn").value("Inactive"),
                jsonPath("$[2].currentStatusEn").doesNotHaveJsonPath()
            );
    }

    @Test
    @DirtiesContext
    void should_create_hearing_fee_unpaid_trial_ready_scenario() throws Exception {

        String caseId = "14323242";
        LocalDate hearingDueDate = LocalDate.now().minusDays(1);
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyApplicant().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The claim has been struck out"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">This is because the hearing fee was not paid by "
                        + DateUtils.formatDate(hearingDueDate)
                        + " as stated in the <a href=\"{VIEW_HEARING_NOTICE}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">hearing notice</a>.</p>"),
                jsonPath("$[0].titleCy").value("Mae'r hawliad wedi cael ei ddileu"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Y rheswm am hyn yw na thalwyd ffi'r gwrandawiad erbyn "
                        + DateUtils.formatDateInWelsh(hearingDueDate)
                        + " fel y nodir yn yr <a href=\"{VIEW_HEARING_NOTICE}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">hysbysiad o wrandawiad</a>.</p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Pay the hearing fee</a>"),
                jsonPath("$[0].currentStatusEn").value("Inactive"),
                jsonPath("$[1].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[1].currentStatusEn").value("Inactive"),
                jsonPath("$[2].currentStatusEn").doesNotHaveJsonPath()
            );
    }
}
