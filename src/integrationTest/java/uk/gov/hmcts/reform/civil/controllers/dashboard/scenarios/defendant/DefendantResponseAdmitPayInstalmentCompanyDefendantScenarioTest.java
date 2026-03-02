package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.DefendantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantResponseAdmitPayInstalmentCompanyDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_part_admit_pay_instalment_company_organisation_scenario() throws Exception {

        String caseId = "12345671";
        LocalDate firstRepaymentDate = OffsetDateTime.now().toLocalDate();
        PaymentFrequencyLRspec frequency = PaymentFrequencyLRspec.ONCE_ONE_WEEK;

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL)
                    .individualFirstName("Claimant")
                    .individualLastName("John")
                    .build())
            .respondent1(PartyBuilder.builder().company().build())
            .respondent1Represented(YesOrNo.NO)
            .respondent1RepaymentPlan(new RepaymentPlanLRspec()
                                          .setFirstRepaymentDate(firstRepaymentDate)
                                          .setPaymentAmount(new BigDecimal(1000))
                                          .setRepaymentFrequency(frequency)
                                          )
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .claimInterest(YesOrNo.NO)
            .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You have offered to pay Claimant John £1000 plus the claim fee and any fixed costs claimed " +
                        "in instalments of £10 " +
                        frequency.getDashboardLabel() + "." +
                        " You have offered to do this starting from " + DateUtils.formatDate(firstRepaymentDate) + "." +
                        " You need to send the claimant your financial details. The court will contact you when they respond.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">View your response</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydych wedi cynnig talu £1000 ynghyd â ffi’r hawliad ac unrhyw gostau sefydlog a hawlir " +
                        "mewn rhandaliadau o £10 i Claimant John " +
                        frequency.getDashboardLabelWelsh() + "." +
                        " Rydych wedi cynnig gwneud hyn o " + DateUtils.formatDateInWelsh(firstRepaymentDate, false) + " ymlaen." +
                        " Mae angen i chi anfon eich manylion ariannol at yr hawlydd. Bydd y llys yn cysylltu â chi pan fyddant yn ymateb.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">Gweld eich ymateb</a></p>"
                )
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName())
            );
    }

    @Test
    void should_create_full_admit_pay_instalment_company_organisation_scenario() throws Exception {

        String caseId = "12345672";
        LocalDate firstRepaymentDate = OffsetDateTime.now().toLocalDate();
        PaymentFrequencyLRspec frequency = PaymentFrequencyLRspec.ONCE_ONE_WEEK;
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1(PartyBuilder.builder().company().build())
            .respondent1Represented(YesOrNo.NO)
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL)
                    .individualFirstName("Claimant")
                    .individualLastName("John")
                    .build())
            .respondent1RepaymentPlan(new RepaymentPlanLRspec()
                                          .setFirstRepaymentDate(firstRepaymentDate)
            .setPaymentAmount(new BigDecimal(1000))
            .setRepaymentFrequency(frequency)
            )
            .totalClaimAmount(new BigDecimal(1000))
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You have offered to pay Claimant John £1001 (this amount includes interest if it has been" +
                        " claimed which may continue to accrue to the date of Judgment, settlement agreement or earlier payment) in instalments" +
                        " of £10 " +
                        frequency.getDashboardLabel() + "." +
                        " You have offered to do this starting from " + DateUtils.formatDate(firstRepaymentDate) + "." +
                        " You need to send the claimant your financial details. The court will contact you when they respond.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">View your response</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydych wedi cynnig talu £1001 (mae’r swm hwn yn cynnwys llog os yw wedi’i hawlio a gall" +
                        " barhau i gronni hyd dyddiad y Dyfarniad, y cytundeb setlo neu daliad cynharach) mewn rhandaliadau o £10 i Claimant John " +
                        frequency.getDashboardLabelWelsh() + "." +
                        " Rydych wedi cynnig gwneud hyn o " + DateUtils.formatDateInWelsh(firstRepaymentDate, false) + " ymlaen." +
                        " Mae angen i chi anfon eich manylion ariannol at yr hawlydd. Bydd y llys yn cysylltu â chi pan fyddant yn ymateb.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">Gweld eich ymateb</a></p>"
                )
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName())
            );
    }
}
