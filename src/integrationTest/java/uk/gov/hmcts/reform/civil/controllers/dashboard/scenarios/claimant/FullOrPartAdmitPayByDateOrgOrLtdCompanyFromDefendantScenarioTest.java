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

public class FullOrPartAdmitPayByDateOrgOrLtdCompanyFromDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseClaimantNotificationHandler handler;

    @Test
    void should_create_part_admit_pay_by_setDate_scenario() throws Exception {

        String caseId = "50320";
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .responseClaimTrack("SMALL_CLAIM")
            .respondent1(Party.builder()
                        .companyName("Company one")
                        .type(Party.Type.COMPANY).build())
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(responseDeadline)
                                               )
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Company one has offered to pay you £1000 plus the claim fee by "
                      +  DateUtils.formatDate(responseDeadline)
                      +  ". The defendant needs to send you their financial details.</p>"
                      +  "<p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">View and respond</a></p>"),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae Company one wedi cynnig talu £1000 ynghyd â ffi’r hawliad i chi erbyn "
                     +   DateUtils.formatDateInWelsh(responseDeadline, false)
                     +   ". Mae angen i’r diffynnydd anfon eu manylion ariannol atoch.</p>"
                     +   "<p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">Gweld ac ymateb</a></p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">Gweld yr ymateb i'r hawliad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.AVAILABLE.getWelshName()));
    }

    @Test
    void should_create_full_admit_pay_by_setDate_scenario() throws Exception {

        String caseId = "50321";
        LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .responseClaimTrack("SMALL_CLAIM")
            .respondent1(Party.builder()
                    .organisationName("Org one")
                    .type(Party.Type.ORGANISATION).build())
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(admitPaymentDeadline)
                                               )
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
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
                    "<p class=\"govuk-body\">Org one has offered to pay you £1001 (this amount includes interest if it has been claimed" +
                        " which may continue to accrue to the date of Judgment, settlement agreement or earlier payment) by "
                        +  DateUtils.formatDate(admitPaymentDeadline)
                        +  ". The defendant needs to send you their financial details.</p>"
                        +  "<p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">View and respond</a></p>"),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae Org one wedi cynnig talu £1001 (mae’r swm hwn yn cynnwys llog os yw wedi’i hawlio a " +
                        "gall barhau i gronni hyd dyddiad y Dyfarniad, y cytundeb setlo neu daliad cynharach) i chi erbyn "
                        +   DateUtils.formatDateInWelsh(admitPaymentDeadline, false)
                        +   ". Mae angen i’r diffynnydd anfon eu manylion ariannol atoch.</p>"
                        +   "<p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">Gweld ac ymateb</a></p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">Gweld yr ymateb i'r hawliad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.AVAILABLE.getWelshName()));
    }
}
