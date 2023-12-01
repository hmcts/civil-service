package uk.gov.hmcts.reform.civil.service.docmosis.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils.getClaimantFinalRepaymentDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils.getClaimantSuggestedRepaymentType;
import static uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils.getDefendantFinalRepaymentDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils.getDefendantRepaymentOption;

@ExtendWith(MockitoExtension.class)
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

    @Test
    void shouldReturnDefendantFinalRepaymentDateWhenPartAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1RepaymentPlan(RepaymentPlanLRspec.builder().repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
                                          .firstRepaymentDate(LocalDate.of(2024, 1, 1))
                                          .paymentAmount(BigDecimal.valueOf(10000)).build())
            .issueDate(LocalDate.now())
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(800))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        LocalDate finalRepaymentDate = getDefendantFinalRepaymentDate(caseData);
        assertThat(finalRepaymentDate).isNotNull();
    }

    @Test
    void shouldReturnDefendantFinalRepaymentDateWhenFullAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1RepaymentPlan(RepaymentPlanLRspec.builder().repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
                                          .firstRepaymentDate(LocalDate.of(2024, 1, 1))
                                          .paymentAmount(new BigDecimal(10000)).build())
            .issueDate(LocalDate.now())
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        LocalDate finalRepaymentDate = getDefendantFinalRepaymentDate(caseData);
        assertThat(finalRepaymentDate).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({"IMMEDIATELY,Immediately", "BY_SET_DATE,By a set date", "SUGGESTION_OF_REPAYMENT_PLAN,By installments"})
    void shouldReturnDefendantRepaymentOption(RespondentResponsePartAdmissionPaymentTimeLRspec input, String expectedOutput) {
        CaseData caseData = CaseData.builder()
            .defenceAdmitPartPaymentTimeRouteRequired(input)
            .build();

        String actualOutput = getDefendantRepaymentOption(caseData);
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @ParameterizedTest
    @CsvSource({"IMMEDIATELY,Immediately", "SET_DATE,By a set date", "REPAYMENT_PLAN,By installments"})
    void shouldReturnClaimantRepaymentOption(PaymentType input, String expectedOutput) {
        CaseData caseData = CaseData.builder()
            .applicant1RepaymentOptionForDefendantSpec(input)
            .build();

        String actualOutput = getClaimantSuggestedRepaymentType(caseData);
        Assertions.assertEquals(expectedOutput, actualOutput);
    }
}
