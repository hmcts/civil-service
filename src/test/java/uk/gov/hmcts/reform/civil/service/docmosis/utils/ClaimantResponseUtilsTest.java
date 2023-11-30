package uk.gov.hmcts.reform.civil.service.docmosis.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.params.ParameterizedTest;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils.getClaimantFinalRepaymentDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils.getClaimantRepaymentType;

public class ClaimantResponseUtilsTest {

    @Test
    void shouldReturnFinalPaymentDateForDefendant() {
        CaseData caseData = CaseDataBuilder.builder()
                .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(BigDecimal.valueOf(100))
                .issueDate(LocalDate.now())
                .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
                .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.now())
                .applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec(PaymentFrequencyClaimantResponseLRspec.ONCE_TWO_WEEKS)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .build();

        LocalDate finalDate = getClaimantFinalRepaymentDate(caseData);
        assertThat(finalDate).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({"IMMEDIATELY,Immediately", "SET_DATE,By a set date", "REPAYMENT_PLAN,By installments"})
    void ShouldReturnClaimantRepaymentOption(PaymentType input, String expectedOutput) {
        CaseData caseData = CaseData.builder()
                .applicant1RepaymentOptionForDefendantSpec(input)
                .build();

        String actualOutput = getClaimantRepaymentType(caseData);
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void shouldNotReturnFinalPaymentDateForDefendant_WhenInstallmentIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
                .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(null)
                .issueDate(LocalDate.now())
                .applicant1RepaymentOptionForDefendantSpec(PaymentType.SET_DATE)
                .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.now())
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .build();

        LocalDate finalDate = getClaimantFinalRepaymentDate(caseData);
        assertThat(finalDate).isNull();
    }
}
