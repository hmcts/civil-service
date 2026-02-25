package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseClaimantNotificationHandler;
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

public class DefendantResponseFullAdmitPayImmediatelyClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseClaimantNotificationHandler handler;

    @Test
    void should_create_full_admit_pay_immediately_claimant_scenario() throws Exception {

        String caseId = "12348";
        LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .responseClaimTrack("SMALL_CLAIM")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                    .individualFirstName("James")
                    .individualLastName("John")
                    .build())
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(admitPaymentDeadline)
                                               )
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .totalClaimAmount(new BigDecimal(1000))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">James John has offered to pay £1001 by " +
                        DateUtils.formatDate(admitPaymentDeadline) + ". This amount includes interest if it has been claimed which may continue" +
                        " to accrue to the date of Judgment, settlement agreement or earlier payment.</p>" +
                        "<p class=\"govuk-body\">The payment must be received in your account by then, if not you can request a county court" +
                        " judgment.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{TELL_US_IT_IS_SETTLED}\" rel=\"noopener noreferrer\" class=\"govuk-link\">" +
                        "Tell us you've settled the claim</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae James John wedi cynnig talu £1001 erbyn " +
                        DateUtils.formatDateInWelsh(admitPaymentDeadline, false) + ". Mae’r swm hwn yn cynnwys llog os yw wedi’i hawlio a " +
                        "gall barhau i gronni hyd dyddiad y Dyfarniad, y cytundeb setlo neu daliad cynharach.</p>" +
                        "<p class=\"govuk-body\">Rhaid i’r taliad fod yn eich cyfrif erbyn y dyddiad hwnnw. Os nad yw, yna gallwch wneud cais" +
                        " am ddyfarniad llys sirol.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{TELL_US_IT_IS_SETTLED}\" rel=\"noopener noreferrer\" class=\"govuk-link\">" +
                        "Tell us you've settled the claim</a></p>"
                )
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} rel=\"noopener noreferrer\" class=\"govuk-link\">" +
                        "View the response to the claim</a>"),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} rel=\"noopener noreferrer\" class=\"govuk-link\">" +
                        "Gweld yr ymateb i'r hawliad</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName())
            );
    }
}
