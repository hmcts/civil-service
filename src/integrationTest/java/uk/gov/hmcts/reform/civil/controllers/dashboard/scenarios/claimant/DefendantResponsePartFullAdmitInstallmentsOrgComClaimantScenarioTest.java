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
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().type(Party.Type.COMPANY).build())
            .respondent1RepaymentPlan(RepaymentPlanLRspec
                                          .builder()
                                          .repaymentFrequency(frequency)
                                          .paymentAmount(installmentAmount)
                                          .firstRepaymentDate(firstPaymentDate)
                                          .build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .totalClaimAmount(totalAmount)
            .build();

        handler.handle(callbackParams(caseData));
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The defendant has offered to pay "
                        + "£" + totalAmount + " in "
                        + "instalments of £"
                        + MonetaryConversions.penniesToPounds(installmentAmount).toPlainString().replace(
                        ".00", "")
                        + " " + frequency.getDashboardLabel() + ".They are offering to do this starting from "
                        + DateUtils.formatDate(firstPaymentDate)
                        + ".</p><p class=\"govuk-body\">The defendant needs to send you their financial details.</p>"
                        + " <a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" rel=\"noopener noreferrer\" class=\"govuk-link\">View and"
                        + " respond</a>"
                )
            );

    }
}
