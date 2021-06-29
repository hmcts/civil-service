package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class InterestCalculatorTest {

    private final InterestCalculator interestCalculator = new InterestCalculator();

    @Test
    public void shouldReturnValidInterestAmountByDate() {
        assertThat(interestCalculator.calculateInterestByDate(
            new BigDecimal("1000"),
            BigDecimal.valueOf(8),
            LocalDate.now().minusDays(2))).isEqualTo("0.44");
    }

    @Test
    public void shouldReturnZeroInterestRateWhenNoInterestIsSelected() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .claimInterest(YesOrNo.NO)
            .totalClaimAmount(BigDecimal.valueOf(5000))
            .build();

        BigDecimal actual = interestCalculator.calculateInterest(caseData);
        assertThat(actual).isZero();
    }

    @Test
    public void shouldReturnZeroInterestRateWhenSameRateInterestAndSubmitDateIsChoosen() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .claimInterest(YesOrNo.YES)
            .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
            .sameRateInterestSelection(SameRateInterestSelection.builder()
                                           .sameRateInterestType(SameRateInterestType.SAME_RATE_INTEREST_8_PC).build())
            .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
            .totalClaimAmount(BigDecimal.valueOf(5000))
            .build();

        BigDecimal actual = interestCalculator.calculateInterest(caseData);
        assertThat(actual).isZero();
    }

    @Test
    public void shouldReturnZeroInterestRateWhenSameRateInterestDifferentRateAndSubmitDateIsChoosen() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .claimInterest(YesOrNo.YES)
            .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
            .sameRateInterestSelection(SameRateInterestSelection.builder()
                                           .sameRateInterestType(SameRateInterestType
                                                                     .SAME_RATE_INTEREST_DIFFERENT_RATE)
                                           .differentRate(BigDecimal.valueOf(10)).build())
            .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
            .totalClaimAmount(BigDecimal.valueOf(5000))
            .build();

        BigDecimal actual = interestCalculator.calculateInterest(caseData);
        assertThat(actual).isZero();
    }

    @Test
    public void shouldReturnZeroInterestRateWhenSameRateInterestDifferentRateAndSpecificDateIsChoosen() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .claimInterest(YesOrNo.YES)
            .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
            .sameRateInterestSelection(SameRateInterestSelection.builder()
                                           .sameRateInterestType(SameRateInterestType
                                                                     .SAME_RATE_INTEREST_DIFFERENT_RATE)
                                           .differentRate(BigDecimal.valueOf(10)).build())
            .interestClaimFrom(InterestClaimFromType.FROM_A_SPECIFIC_DATE)
            .interestClaimUntil(InterestClaimUntilType.UNTIL_CLAIM_SUBMIT_DATE)
            .interestFromSpecificDate(LocalDate.now().minusDays(1))
            .totalClaimAmount(BigDecimal.valueOf(5000))
            .build();

        BigDecimal actual = interestCalculator.calculateInterest(caseData);
        assertThat(actual).isEqualTo(BigDecimal.valueOf(1.37));
    }

    @Test
    public void shouldReturnValidInterestRateWhenSameRateInterestAndSpecificDateIsChoosen() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .claimInterest(YesOrNo.YES)
            .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
            .sameRateInterestSelection(SameRateInterestSelection.builder()
                                           .sameRateInterestType(SameRateInterestType.SAME_RATE_INTEREST_8_PC).build())
            .interestClaimFrom(InterestClaimFromType.FROM_A_SPECIFIC_DATE)
            .interestClaimUntil(InterestClaimUntilType.UNTIL_CLAIM_SUBMIT_DATE)
            .interestFromSpecificDate(LocalDate.now().minusDays(1))
            .totalClaimAmount(BigDecimal.valueOf(5000))
            .build();

        BigDecimal actual = interestCalculator.calculateInterest(caseData);
        assertThat(actual).isGreaterThanOrEqualTo(BigDecimal.valueOf(1.10));
    }

    @Test
    public void shouldReturnZeroInterestRateWhenDifferentRateInterestAndSubmitDateIsChoosen() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .claimInterest(YesOrNo.YES)
            .interestClaimOptions(InterestClaimOptions.BREAK_DOWN_INTEREST)
            .breakDownInterestTotal(BigDecimal.valueOf(500))
            .build();

        BigDecimal actual = interestCalculator.calculateInterest(caseData);
        assertThat(actual).isGreaterThanOrEqualTo(BigDecimal.valueOf(500));
    }
}
