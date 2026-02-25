package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PartAdmitFullDefenceFullPaidClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseClaimantNotificationHandler handler;

    @Test
    void should_create_full_defence_full_paid_claimant_scenario() throws Exception {

        String caseId = "123456731";
        LocalDate paymentDate = LocalDate.of(2024, 3, 27);
        LocalDate deadline = LocalDate.of(2024, 7, 25);
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build()
            .toBuilder()
            .responseClaimTrack("SMALL_CLAIM")
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1ResponseDeadline(LocalDateTime.of(deadline, LocalTime.now()))
            .responseClaimTrack("SMALL_CLAIM")
            .respondToClaim(new RespondToClaim()
                                .setHowMuchWasPaid(new BigDecimal(100000))
                                .setWhenWasThisAmountPaid(paymentDate)
                                )
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
                    "<p class=\"govuk-body\">The defendant has said they already paid £1000 " +
                        "on " + DateUtils.formatDate(paymentDate) + ". You can confirm " +
                        "payment and settle, or proceed with the claim. You need to respond " +
                        "by 4pm on " + DateUtils.formatDate(deadline) + " or the claim will not continue.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">View and respond</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae’r diffynnydd wedi dweud eu bod wedi talu £1000 yn barod ar " +
                         DateUtils.formatDateInWelsh(paymentDate, false) +
                        ". Gallwch gadarnhau bod y taliad wedi’i wneud a setlo, neu barhau â’r hawliad." +
                        " Mae angen i chi ymateb erbyn 4pm ar " + DateUtils.formatDateInWelsh(deadline, false) + " neu ni fydd yr hawliad yn parhau.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">Gweld ac ymateb</a></p>"
                )
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName())
            );
    }

    @Test
    void should_create_part_admit_full_paid_claimant_scenario() throws Exception {

        String caseId = "123456732";
        LocalDate paymentDate = LocalDate.of(2024, 3, 27);
        LocalDate deadline = LocalDate.of(2024, 7, 25);

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .responseClaimTrack("SMALL_CLAIM")
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .applicant1ResponseDeadline(LocalDateTime.of(deadline, LocalTime.now()))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondToAdmittedClaim(new RespondToClaim()
                                        .setHowMuchWasPaid(new BigDecimal(100000))
                                        .setWhenWasThisAmountPaid(paymentDate)
                                        )
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
                    "<p class=\"govuk-body\">The defendant has said they already paid £1000 " +
                        "on " + DateUtils.formatDate(paymentDate) + ". You can confirm " +
                        "payment and settle, or proceed with the claim. You need to respond " +
                        "by 4pm on " + DateUtils.formatDate(deadline) + " or the claim will not continue.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">View and respond</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae’r diffynnydd wedi dweud eu bod wedi talu £1000 yn barod ar " +
                        DateUtils.formatDateInWelsh(paymentDate, false) +
                        ". Gallwch gadarnhau bod y taliad wedi’i wneud a setlo, neu barhau â’r hawliad." +
                        " Mae angen i chi ymateb erbyn 4pm ar " + DateUtils.formatDateInWelsh(deadline, false) + " neu ni fydd yr hawliad yn parhau.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">Gweld ac ymateb</a></p>"
                )
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName())
            );
    }

}
