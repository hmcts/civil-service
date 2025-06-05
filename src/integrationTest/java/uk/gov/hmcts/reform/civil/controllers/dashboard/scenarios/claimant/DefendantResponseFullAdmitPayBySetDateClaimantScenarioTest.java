package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.DefendantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantResponseFullAdmitPayBySetDateClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_full_admit_pay_setdate_claimant_scenario() throws Exception {

        String caseId = "12348";
        LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .responseClaimTrack("SMALL_CLAIM")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                .individualFirstName("James")
                .individualLastName("John")
                .build())
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec
                .builder()
                .whenWillThisAmountBePaid(admitPaymentDeadline)
                .build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .totalClaimAmount(new BigDecimal(1000))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You have offered to pay £1001 (this amount includes interest if it has been claimed " +
                        "which will continue to accrue to the date of Judgment, settlement agreement or earlier payment) by " +
                        DateUtils.formatDate(admitPaymentDeadline) + ". We will contact you when the claimant responds to your offer." +
                        "</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">View your response</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydych wedi cynnig talu £1001 (mae’r swm hwn yn cynnwys llog os hawlir a fydd yn parhau" +
                        " i gronni hyd at ddyddiad y dyfarniad, cytundeb setlo neu daliad cynharach) erbyn " +
                        DateUtils.formatDateInWelsh(admitPaymentDeadline, false) + ". Byddwn yn cysylltu â chi pan fydd yr hawlydd" +
                        " yn ymateb i’ch cynnig.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">" +
                        "Gweld eich ymateb</a></p>"
                )
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">Gweld yr ymateb i'r hawliad</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName())
            );
    }
}
