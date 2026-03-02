package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantResponsePartFullAdmitInstallmentsOrgComClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseClaimantNotificationHandler handler;

    @Test
    void should_create_full_part_admit_installments_org_com_claimant_scenario() throws Exception {

        String caseId = "10002348";
        LocalDate firstPaymentDate = LocalDate.now();
        PaymentFrequencyLRspec frequency = PaymentFrequencyLRspec.ONCE_FOUR_WEEKS;
        BigDecimal installmentAmount = new BigDecimal("100");
        BigDecimal totalAmount = new BigDecimal("10000");
        BigDecimal partAdmittedAmount = new BigDecimal("9000");
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .responseClaimTrack("SMALL_CLAIM")
            .respondent1(Party.builder().companyName("Company One").type(Party.Type.COMPANY).build())
            .respondent1RepaymentPlan(new RepaymentPlanLRspec()
                                          .setRepaymentFrequency(frequency)
                                          .setPaymentAmount(installmentAmount)
                                          .setFirstRepaymentDate(firstPaymentDate)
                                          )
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .claimInterest(YesOrNo.NO)
            .totalClaimAmount(totalAmount)
            .respondToAdmittedClaimOwingAmountPounds(partAdmittedAmount)
            .build();

        handler.handle(callbackParams(caseData));
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Company One has offered to pay you "
                        + "£" + partAdmittedAmount + " plus the claim fee in "
                        + "instalments of £"
                        + MonetaryConversions.penniesToPounds(installmentAmount).toPlainString().replace(
                        ".00", "")
                        + " " + frequency.getDashboardLabel() + ". They are offering to do this starting from "
                        + DateUtils.formatDate(firstPaymentDate)
                        + ". The defendant needs to send you their financial details.</p>"
                        + "<p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" rel=\"noopener noreferrer\" class=\"govuk-link\">View and"
                        + " respond</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae Company One wedi cynnig talu "
                        + "£" + partAdmittedAmount
                        + " ynghyd â ffi’r hawliad i chi mewn rhandaliadau o £"
                        + MonetaryConversions.penniesToPounds(installmentAmount).toPlainString().replace(
                        ".00", "")
                        + " " + frequency.getDashboardLabelWelsh() + ". Maent yn cynnig gwneud hyn o "
                        + DateUtils.formatDateInWelsh(firstPaymentDate, false)
                        + " ymlaen. Mae angen i’r diffynnydd anfon eu manylion ariannol atoch.</p>"
                        + "<p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld ac ymateb"
                        + "</a></p>"
                )
            );
    }
}
