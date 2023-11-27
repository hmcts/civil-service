package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InterestCalculatorTest {

    @Mock
    private Time time;
    @InjectMocks
    private InterestCalculator interestCalculator;

    @Test
    public void shouldReturnValidInterestAmountByDate() {
        LocalDateTime dateTime = LocalDateTime.now().withHour(13).withMinute(59);
        when(time.now()).thenReturn(dateTime);
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
        LocalDateTime dateTime = LocalDateTime.of(2022, 11, 15, 13, 0);
        when(time.now()).thenReturn(dateTime);
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
        LocalDateTime dateTime = LocalDateTime.of(2022, 11, 15, 13, 0);
        when(time.now()).thenReturn(dateTime);
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
        LocalDateTime dateTime = LocalDateTime.now().withHour(13).withMinute(59);
        when(time.now()).thenReturn(dateTime);
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .claimInterest(YesOrNo.YES)
            .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
            .sameRateInterestSelection(SameRateInterestSelection.builder()
                                           .sameRateInterestType(SameRateInterestType
                                                                     .SAME_RATE_INTEREST_DIFFERENT_RATE)
                                           .differentRate(BigDecimal.valueOf(10)).build())
            .interestClaimFrom(InterestClaimFromType.FROM_A_SPECIFIC_DATE)
            .interestClaimUntil(InterestClaimUntilType.UNTIL_CLAIM_SUBMIT_DATE)
            .interestFromSpecificDate(LocalDate.now())
            .totalClaimAmount(BigDecimal.valueOf(5000))
            .build();

        BigDecimal actual = interestCalculator.calculateInterest(caseData);
        assertThat(actual).isZero();
    }

    @Test
    public void shouldReturnValidInterestRateWhenSameRateInterestAndSpecificDateIsChoosen() {
        LocalDateTime dateTime = LocalDateTime.now().withHour(13).withMinute(59);
        when(time.now()).thenReturn(dateTime);
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .claimInterest(YesOrNo.YES)
            .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
            .sameRateInterestSelection(SameRateInterestSelection.builder()
                                           .sameRateInterestType(SameRateInterestType.SAME_RATE_INTEREST_8_PC).build())
            .interestClaimFrom(InterestClaimFromType.FROM_A_SPECIFIC_DATE)
            .interestClaimUntil(InterestClaimUntilType.UNTIL_CLAIM_SUBMIT_DATE)
            .interestFromSpecificDate(LocalDate.now().minusDays(6))
            .totalClaimAmount(BigDecimal.valueOf(5000))
            .build();

        BigDecimal actual = interestCalculator.calculateInterest(caseData);
        assertThat(actual).isEqualTo(BigDecimal.valueOf(6.60).setScale(2, RoundingMode.UNNECESSARY));
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

    @Test
    public void shouldReturnInterestRateBulkClaim_InterestSelectedBefore4pm() {
        // when before 4pm interest will be days multiplied by daily rate of interest. 5 * 6 = 30
        LocalDateTime dateTime = LocalDateTime.of(2023, 11, 15, 15, 0);
        when(time.now()).thenReturn(dateTime);
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .claimInterest(YesOrNo.YES)
            .interestFromSpecificDate(LocalDate.of(2023, 11, 20))
            .sameRateInterestSelection(SameRateInterestSelection.builder()
                                           .differentRate(BigDecimal.valueOf(6L))
                                           .build())
            .build();

        BigDecimal result = interestCalculator.calculateBulkInterest(caseData);

        assertThat(result).isEqualTo(BigDecimal.valueOf(30));
    }

    @Test
    public void shouldReturnInterestRateBulkClaim_InterestSelectedAfter4pm() {
        // when after 4pm interest will be days +1, multiplied by daily rate of interest. (5+1) * 6 = 36
        LocalDateTime dateTime = LocalDateTime.of(2023, 11, 15, 18, 0);
        when(time.now()).thenReturn(dateTime);
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .claimInterest(YesOrNo.YES)
            .interestFromSpecificDate(LocalDate.of(2023, 11, 20))
            .sameRateInterestSelection(SameRateInterestSelection.builder()
                                           .differentRate(BigDecimal.valueOf(6L))
                                           .build())
            .build();

        BigDecimal result = interestCalculator.calculateBulkInterest(caseData);

        assertThat(result).isEqualTo(BigDecimal.valueOf(36));
    }

    @Test
    public void shouldReturnZeroInterestRateBulkClaim_noInterestSelected() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .claimInterest(YesOrNo.NO)
            .interestFromSpecificDate(null)
            .sameRateInterestSelection(null)
            .build();

        BigDecimal result = interestCalculator.calculateBulkInterest(caseData);
        assertThat(result).isZero();
    }
}
