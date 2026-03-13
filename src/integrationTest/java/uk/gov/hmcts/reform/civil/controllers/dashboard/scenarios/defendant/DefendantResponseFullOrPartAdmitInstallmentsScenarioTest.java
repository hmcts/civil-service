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
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantResponseFullOrPartAdmitInstallmentsScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_full_or_part_admit_installments_scenario() throws Exception {

        String caseId = "19923451432";
        LocalDate firstPaymentDate = LocalDate.now();
        PaymentFrequencyLRspec frequency = PaymentFrequencyLRspec.ONCE_FOUR_WEEKS;
        BigDecimal installmentAmount = new BigDecimal("100");
        BigDecimal totalAmount = new BigDecimal("10000");

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .respondent1RepaymentPlan(new RepaymentPlanLRspec()
                                          .setRepaymentFrequency(frequency)
                                          .setPaymentAmount(installmentAmount)
                                          .setFirstRepaymentDate(firstPaymentDate)
                                          )
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondToAdmittedClaimOwingAmountPounds(totalAmount)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You have offered to pay £" + totalAmount
                        + " plus the claim fee and any fixed costs claimed in instalments of £"
                        + MonetaryConversions.penniesToPounds(installmentAmount).toPlainString().replace(
                        ".00", "")
                        + " " + frequency.getDashboardLabel() + ". You have offered to do this starting from " + DateUtils.formatDate(firstPaymentDate)
                        + ". We will contact you when the claimant responds to your offer.</p><p class=\"govuk-body\"><a "
                        + "href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">View your response</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydych wedi cynnig talu £" + totalAmount
                        + " ynghyd â ffi’r hawliad ac unrhyw gostau sefydlog a hawlir mewn rhandaliadau o £"
                        + MonetaryConversions.penniesToPounds(installmentAmount).toPlainString().replace(
                        ".00", "")
                        + " " + frequency.getDashboardLabelWelsh() + ". Rydych wedi cynnig gwneud hyn o " + DateUtils.formatDateInWelsh(firstPaymentDate, false)
                        + " ymlaen. Byddwn yn cysylltu â chi pan fydd yr hawlydd yn ymateb i’ch cynnig.</p><p class=\"govuk-body\"><a "
                        + "href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">Gweld eich ymateb</a></p>"
                )
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>")

            );
    }
}
