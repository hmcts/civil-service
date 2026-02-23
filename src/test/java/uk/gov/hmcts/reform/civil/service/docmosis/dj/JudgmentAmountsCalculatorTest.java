package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FixedCosts;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper.getPartialPayment;

@ExtendWith(MockitoExtension.class)
class JudgmentAmountsCalculatorTest {

    @Mock
    private InterestCalculator interestCalculator;

    @InjectMocks
    private JudgmentAmountsCalculator judgmentAmountsCalculator;

    @Test
    void shouldGetPartialPaymentInPoundsFromCaseData() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued().build().toBuilder()
            .partialPaymentAmount("15000")
            .build();

        assertThat(getPartialPayment(caseData)).isEqualTo("150.00");
    }

    @Test
    void shouldReturnClaimFeeWithOutstandingFee_whenHelpWithFeesIsTrue() {

        CaseData caseData = CaseDataBuilder.builder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .claimFee(new Fee().setCalculatedAmountInPence(new BigDecimal(1000)))
            .claimIssuedHwfDetails(new HelpWithFeesDetails().setOutstandingFeeInPounds(BigDecimal.valueOf(50)))
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .totalClaimAmount(new BigDecimal(2000))
            .caseDataLip(new CaseDataLiP().setHelpWithFees(new HelpWithFees().setHelpWithFee(YesOrNo.YES)))
            .build();

        BigDecimal claimFee = judgmentAmountsCalculator.getClaimFee(caseData);

        assertThat(claimFee).isEqualTo("50.00");
    }

    @Test
    void shouldReturnClaimFeeWithFixedCosts_whenFixedCostsAreProvided() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .paymentConfirmationDecisionSpec(YesOrNo.YES)
            .fixedCosts(new FixedCosts()
                .setFixedCostAmount("1000")
                .setClaimFixedCosts(YesOrNo.YES)
                )
            .claimFee(new Fee().setCalculatedAmountInPence(new BigDecimal("1000")))
            .build();

        BigDecimal claimFee = judgmentAmountsCalculator.getClaimFee(caseData);

        assertThat(claimFee).isEqualTo("20.00");
    }

    @Test
    void shouldReturnClaimFeeWithCalculatedFixedCostsOnDJEntry_whenFixedCostsAreProvided() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .paymentConfirmationDecisionSpec(YesOrNo.YES)
            .totalClaimAmount(new BigDecimal("5000"))
            .fixedCosts(new FixedCosts()
                .setFixedCostAmount("1000")
                .setClaimFixedCosts(YesOrNo.YES)
                )
            .claimFixedCostsOnEntryDJ(YesOrNo.YES)
            .claimFee(new Fee().setCalculatedAmountInPence(new BigDecimal("9000")))
            .build();

        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(new BigDecimal("50.00"));

        BigDecimal claimFee = judgmentAmountsCalculator.getClaimFee(caseData);

        assertThat(claimFee).isEqualTo("130.00");
    }

    @Test
    void shouldReturnClaimFeeWithFixedCosts_whenFixedCostsAreProvidedAndPaymentConfirmationDecisionNo() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .paymentConfirmationDecisionSpec(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal("5000"))
            .fixedCosts(new FixedCosts()
                .setFixedCostAmount("1000")
                .setClaimFixedCosts(YesOrNo.YES)
                )
            .claimFixedCostsOnEntryDJ(YesOrNo.NO)
            .claimFee(new Fee().setCalculatedAmountInPence(new BigDecimal("8000")))
            .build();

        BigDecimal claimFee = judgmentAmountsCalculator.getClaimFee(caseData);

        assertThat(claimFee).isEqualTo("90.00");
    }

    @Test
    void shouldReturnDebtAmountWithInterest_whenInterestIsCalculated() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .totalClaimAmount(new BigDecimal("1000"))
            .build();
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(new BigDecimal("50.00"));

        BigDecimal debtAmount = judgmentAmountsCalculator.getDebtAmount(caseData);

        assertThat(debtAmount).isEqualTo("1050.00");
    }

    @Test
    void shouldReturnDebtAmountWithPartialPaymentDeducted_whenPartialPaymentIsProvided() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .totalClaimAmount(new BigDecimal("1000"))
            .partialPaymentAmount("20000")
            .build();
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(new BigDecimal("50.00"));

        BigDecimal debtAmount = judgmentAmountsCalculator.getDebtAmount(caseData);

        assertThat(debtAmount).isEqualTo("850.00");
    }

    @Test
    void shouldReturnZeroDebtAmount_whenTotalClaimAmountAndInterestAreZero() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .totalClaimAmount(BigDecimal.ZERO)
            .build();
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.ZERO);

        BigDecimal debtAmount = judgmentAmountsCalculator.getDebtAmount(caseData);

        assertThat(debtAmount).isEqualTo(BigDecimal.ZERO);
    }
}
