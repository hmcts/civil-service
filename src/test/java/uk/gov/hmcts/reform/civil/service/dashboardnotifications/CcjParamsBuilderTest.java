package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CcjParamsBuilderTest {

    private CcjParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new CcjParamsBuilder();
    }

    @Test
    void shouldAddParamsForPayImmediately() {
        CaseData caseData = mock(CaseData.class);
        JudgmentDetails judgmentDetails = mock(JudgmentDetails.class);
        JudgmentPaymentPlan paymentPlan = mock(JudgmentPaymentPlan.class);

        when(caseData.getActiveJudgment()).thenReturn(judgmentDetails);
        when(judgmentDetails.getState()).thenReturn(JudgmentState.ISSUED);
        when(judgmentDetails.getPaymentPlan()).thenReturn(paymentPlan);
        when(paymentPlan.getType()).thenReturn(PaymentPlanSelection.PAY_IMMEDIATELY);
        when(judgmentDetails.getTotalAmount()).thenReturn("10000");

        HashMap<String, Object> params = new HashMap<>();
        builder.addParams(caseData, params);

        assertThat(params).containsEntry("ccjDefendantAdmittedAmount", new BigDecimal("100.00"));
        assertThat(params).containsEntry("ccjPaymentMessageEn", "immediately");
        assertThat(params).containsEntry("ccjPaymentMessageCy", "ar unwaith");
    }

    @Test
    void shouldAddParamsForPayInInstalments() {
        CaseData caseData = mock(CaseData.class);
        JudgmentDetails judgmentDetails = mock(JudgmentDetails.class);
        JudgmentPaymentPlan paymentPlan = mock(JudgmentPaymentPlan.class);
        JudgmentInstalmentDetails instalmentDetails = mock(JudgmentInstalmentDetails.class);

        when(caseData.getActiveJudgment()).thenReturn(judgmentDetails);
        when(judgmentDetails.getState()).thenReturn(JudgmentState.ISSUED);
        when(judgmentDetails.getPaymentPlan()).thenReturn(paymentPlan);
        when(paymentPlan.getType()).thenReturn(PaymentPlanSelection.PAY_IN_INSTALMENTS);
        when(judgmentDetails.getInstalmentDetails()).thenReturn(instalmentDetails);
        when(judgmentDetails.getTotalAmount()).thenReturn("20000");
        when(instalmentDetails.getPaymentFrequency()).thenReturn(PaymentFrequency.MONTHLY);
        when(instalmentDetails.getAmount()).thenReturn("2000");
        when(instalmentDetails.getStartDate()).thenReturn(LocalDate.of(2023, 1, 1));

        HashMap<String, Object> params = new HashMap<>();
        builder.addParams(caseData, params);

        assertThat(params).containsEntry("ccjDefendantAdmittedAmount", new BigDecimal("200.00"));
        assertThat(params).containsEntry("ccjPaymentMessageEn", "in monthly instalments of £20.00. The first payment is due on 1 January 2023");
        assertThat(params).containsEntry("ccjPaymentMessageCy", "mewn rhandaliadau misol o £20.00. Bydd y taliad cyntaf yn ddyledus ar 1 Ionawr 2023");
    }

    @Test
    void shouldAddParamsForPayByDeadline() {
        CaseData caseData = mock(CaseData.class);
        JudgmentDetails judgmentDetails = mock(JudgmentDetails.class);
        JudgmentPaymentPlan paymentPlan = mock(JudgmentPaymentPlan.class);

        when(caseData.getActiveJudgment()).thenReturn(judgmentDetails);
        when(judgmentDetails.getState()).thenReturn(JudgmentState.ISSUED);
        when(judgmentDetails.getPaymentPlan()).thenReturn(paymentPlan);
        when(paymentPlan.getType()).thenReturn(PaymentPlanSelection.PAY_IMMEDIATELY);
        when(paymentPlan.getPaymentDeadlineDate()).thenReturn(LocalDate.of(2023, 12, 31));
        when(judgmentDetails.getTotalAmount()).thenReturn("30000");

        HashMap<String, Object> params = new HashMap<>();
        builder.addParams(caseData, params);

        assertThat(params).containsEntry("ccjDefendantAdmittedAmount", new BigDecimal("300.00"));
        assertThat(params).containsEntry("ccjPaymentMessageEn", "immediately");
        assertThat(params).containsEntry("ccjPaymentMessageCy", "ar unwaith");
    }

    @Test
    void shouldNotAddParamsWhenNoActiveJudgment() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getActiveJudgment()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();
        builder.addParams(caseData, params);

        assertThat(params).isEmpty();
    }
}
