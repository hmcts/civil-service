package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentFrequencyParamsBuilderTest {

    private PaymentFrequencyParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new PaymentFrequencyParamsBuilder();
    }

    @Test
    void shouldAddPaymentFrequencyMessagesWhenJudgmentRecordReasonIsDeterminationOfMeans() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        JudgmentInstalmentDetails instalmentDetails = JudgmentInstalmentDetails.builder()
            .startDate(LocalDate.of(2023, 10, 15))
            .paymentFrequency(PaymentFrequency.MONTHLY)
            .amount("1000.00")
            .build();

        when(caseData.getJoJudgmentRecordReason()).thenReturn(JudgmentRecordedReason.DETERMINATION_OF_MEANS);
        when(caseData.getJoPaymentPlan()).thenReturn(JudgmentPaymentPlan.builder().type(PaymentPlanSelection.PAY_IN_INSTALMENTS).build());
        when(caseData.getJoAmountOrdered()).thenReturn("10000");
        when(caseData.getJoAmountCostOrdered()).thenReturn("2000");
        when(caseData.getJoInstalmentDetails()).thenReturn(instalmentDetails);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsKeys("paymentFrequencyMessage", "paymentFrequencyMessageCy");
        assertThat(params.get("paymentFrequencyMessage"))
            .isEqualTo("You must pay the claim amount of £120.00 in monthly instalments of £10.00. The first payment is due on 15 October 2023");
        assertThat(params.get("paymentFrequencyMessageCy")).isEqualTo(
            "Rhaid i chi dalu swm yr hawliad, sef £120.00 mewn rhandaliadau misol o £10.00. Bydd y taliad cyntaf yn ddyledus ar 15 Hydref 2023");
    }

    @Test
    void shouldNotAddPaymentFrequencyMessagesWhenJudgmentRecordReasonIsNotDeterminationOfMeans() {
        // Arrange
        CaseData caseData = mock(CaseData.class);

        when(caseData.getJoJudgmentRecordReason()).thenReturn(JudgmentRecordedReason.JUDGE_ORDER);
        when(caseData.getJoPaymentPlan()).thenReturn(JudgmentPaymentPlan.builder().type(PaymentPlanSelection.PAY_IN_INSTALMENTS).build());

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
