package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.HearingFeeUnpaidDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class HearingFeeUnpaidScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HearingFeeUnpaidDefendantNotificationHandler handler;

    @Test
    void should_create_hearing_fee_unpaid_scenario() throws Exception {

        String caseId = "14323241";
        LocalDate hearingDueDate = LocalDate.now().minusDays(1);
        String dateString = DateUtils.formatDate(hearingDueDate);
        CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDueUnpaid().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The claim has been struck out"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">This is because the hearing fee was not paid by " + dateString + " as stated in the <a href=\"{VIEW_HEARING_NOTICE}\" class=\"govuk-link\">hearing notice.</a></p>"),
                jsonPath("$[0].titleCy").value("The claim has been struck out"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">This is because the hearing fee was not paid by " + dateString + " as stated in the <a href=\"{VIEW_HEARING_NOTICE}\" class=\"govuk-link\">hearing notice.</a></p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[0].currentStatusEn").value("Inactive"),
                jsonPath("$[1].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[1].currentStatusEn").value("Inactive"),
                jsonPath("$[2].currentStatusEn").doesNotHaveJsonPath()
            );
    }

    @Test
    void should_create_hearing_fee_unpaid_trial_ready_scenario() throws Exception {

        String caseId = "14323241";
        LocalDate hearingDueDate = LocalDate.now().minusDays(1);
        String dateString = DateUtils.formatDate(hearingDueDate);
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyRespondent1().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The claim has been struck out"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">This is because the hearing fee was not paid by " + dateString + " as stated in the <a href=\"{VIEW_HEARING_NOTICE}\" class=\"govuk-link\">hearing notice.</a></p>"),
                jsonPath("$[0].titleCy").value("The claim has been struck out"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">This is because the hearing fee was not paid by " + dateString + " as stated in the <a href=\"{VIEW_HEARING_NOTICE}\" class=\"govuk-link\">hearing notice.</a></p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[0].currentStatusEn").value("Inactive"),
                jsonPath("$[1].taskNameEn").doesNotHaveJsonPath()
            );
    }
}
