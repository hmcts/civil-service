package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FixedCosts;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;

@ExtendWith(MockitoExtension.class)
public class ClaimantResponseUtilsTest {

    @Mock
    private InterestCalculator interestCalculator;

    private ClaimantResponseUtils claimantResponseUtils;

    @BeforeEach
    void setUp() {
        claimantResponseUtils = new ClaimantResponseUtils(interestCalculator);
    }

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

        LocalDate finalDate = claimantResponseUtils.getClaimantFinalRepaymentDate(caseData);
        assertThat(finalDate).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({"IMMEDIATELY,Immediately", "SET_DATE,By a set date", "REPAYMENT_PLAN,By instalments"})
    void shouldReturnClaimantRepaymentOption(PaymentType input, String expectedOutput) {
        CaseData caseData = CaseData.builder()
            .applicant1RepaymentOptionForDefendantSpec(input)
            .build();

        String actualOutput = claimantResponseUtils.getClaimantRepaymentType(caseData);
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

        LocalDate finalDate = claimantResponseUtils.getClaimantFinalRepaymentDate(caseData);
        assertThat(finalDate).isNull();
    }

    @Test
    void shouldReturnDefendantFinalRepaymentDateWhenPartAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1RepaymentPlan(new RepaymentPlanLRspec().setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
                                          .setFirstRepaymentDate(LocalDate.of(2024, 1, 1))
                                          .setPaymentAmount(BigDecimal.valueOf(10000)))
            .issueDate(LocalDate.now())
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(800))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        LocalDate finalRepaymentDate = claimantResponseUtils.getDefendantFinalRepaymentDate(caseData);
        assertThat(finalRepaymentDate).isNotNull();
    }

    @Test
    void shouldReturnDefendantFinalRepaymentDateWhenFullAdmission() {
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.TEN);
        CaseData caseData = CaseData.builder()
            .respondent1RepaymentPlan(new RepaymentPlanLRspec().setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
                                          .setFirstRepaymentDate(LocalDate.of(2024, 1, 1))
                                          .setPaymentAmount(new BigDecimal(10000)))
            .issueDate(LocalDate.now())
            .claimFee(new Fee().setCalculatedAmountInPence(new BigDecimal(2000)))
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        LocalDate finalRepaymentDate = claimantResponseUtils.getDefendantFinalRepaymentDate(caseData);
        assertThat(finalRepaymentDate).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({"IMMEDIATELY,Immediately", "BY_SET_DATE,By a set date", "SUGGESTION_OF_REPAYMENT_PLAN,By instalments"})
    void shouldReturnDefendantRepaymentOption(RespondentResponsePartAdmissionPaymentTimeLRspec input, String expectedOutput) {
        CaseData caseData = CaseData.builder()
            .defenceAdmitPartPaymentTimeRouteRequired(input)
            .build();

        String actualOutput = claimantResponseUtils.getDefendantRepaymentOption(caseData);
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void shouldGetTheDefendantAdmittedAmount() {
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.TEN);
        CaseData caseData = CaseData.builder()
            .respondent1RepaymentPlan(new RepaymentPlanLRspec().setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
                                          .setFirstRepaymentDate(LocalDate.of(2024, 1, 1))
                                          .setPaymentAmount(new BigDecimal(10000)))
            .issueDate(LocalDate.now())
            .claimFee(new Fee().setCalculatedAmountInPence(new BigDecimal(2000)))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        BigDecimal actualOutput = claimantResponseUtils.getDefendantAdmittedAmount(caseData);
        assertThat(actualOutput).isNotNull();
    }

    @Test
    void shouldGetTheDefendantAdmittedAmountWithFixedCosts() {
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.TEN);
        CaseData caseData = CaseData.builder()
            .respondent1RepaymentPlan(new RepaymentPlanLRspec().setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
                .setFirstRepaymentDate(LocalDate.of(2024, 1, 1))
                .setPaymentAmount(new BigDecimal(10000)))
            .issueDate(LocalDate.now())
            .claimFee(new Fee().setCalculatedAmountInPence(new BigDecimal(2000)))
            .fixedCosts(new FixedCosts().setClaimFixedCosts(YesOrNo.YES).setFixedCostAmount("5000"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        BigDecimal actualOutput = claimantResponseUtils.getDefendantAdmittedAmount(caseData, true);
        assertThat(actualOutput).isNotNull();
        Assertions.assertEquals(new BigDecimal("1080.00"), actualOutput);
    }

    @Test
    void shouldGetTheDefendantAdmittedAmountWhenPartAdmit() {
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.TEN);
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .claimFee(new Fee().setCalculatedAmountInPence(new BigDecimal(2000)))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(1000))
            .build();

        BigDecimal actualOutput = claimantResponseUtils.getDefendantAdmittedAmount(caseData);
        assertThat(actualOutput).isNotNull();
    }

    @Test
    void shouldGetTheDefendantAdmittedAmountWhenHWF() {
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.TEN);
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .claimFee(new Fee().setCalculatedAmountInPence(new BigDecimal(2000)))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(1000))
            .caseDataLiP(new CaseDataLiP().setHelpWithFees(new HelpWithFees().setHelpWithFee(YesOrNo.YES)))
            .hwfFeeType(FeeType.CLAIMISSUED)
            .fixedCosts(new FixedCosts().setClaimFixedCosts(YesOrNo.NO))
            .claimIssuedHwfDetails(new HelpWithFeesDetails().setOutstandingFeeInPounds(BigDecimal.valueOf(100)))
            .build();

        BigDecimal actualOutput = claimantResponseUtils.getDefendantAdmittedAmount(caseData);
        assertThat(actualOutput).isNotNull();
    }
}
