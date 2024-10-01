package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.Time;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static  uk.gov.hmcts.reform.civil.utils.MonetaryConversions.HUNDRED;

@Component
@RequiredArgsConstructor
public class InterestCalculator {

    public static final int TO_FULL_PENNIES = 2;
    protected static final BigDecimal EIGHT_PERCENT_INTEREST_RATE = valueOf(8);
    private static final String FROM_CLAIM_SUBMIT_DATE = "FROM_CLAIM_SUBMIT_DATE";
    private static final String FROM_SPECIFIC_DATE = "FROM_A_SPECIFIC_DATE";
    private static final String UNTIL_CLAIM_SUBMIT_DATE = "UNTIL_CLAIM_SUBMIT_DATE";
    public static final BigDecimal NUMBER_OF_DAYS_IN_YEAR = new BigDecimal(365L);
    protected static final String SAME_RATE_INTEREST = "SAME_RATE_INTEREST";
    protected static final String SAME_RATE_INTEREST_8_PC = "SAME_RATE_INTEREST_8_PC";
    protected static final String SAME_RATE_INTEREST_DIFFERENT_RATE = "SAME_RATE_INTEREST_DIFFERENT_RATE";
    protected static final String BREAK_DOWN_INTEREST = "BREAK_DOWN_INTEREST";
    public LocalDateTime localDateTime = LocalDateTime.now();
    private final Time time;

    public BigDecimal calculateInterest(CaseData caseData) {
        return this.calculateInterest(caseData, getToDate(caseData));
    }

    private BigDecimal calculateInterest(CaseData caseData, LocalDate interestToDate) {
        BigDecimal interestAmount = ZERO;
        if (caseData.getClaimInterest() == YesOrNo.YES) {
            if (caseData.getInterestClaimOptions().name().equals(SAME_RATE_INTEREST)) {
                if (caseData.getSameRateInterestSelection().getSameRateInterestType().name()
                    .equals(SAME_RATE_INTEREST_8_PC)) {
                    interestAmount = calculateInterestAmount(caseData, EIGHT_PERCENT_INTEREST_RATE, interestToDate);
                }
                if (caseData.getSameRateInterestSelection().getSameRateInterestType().name()
                    .equals(SAME_RATE_INTEREST_DIFFERENT_RATE)) {
                    interestAmount = calculateInterestAmount(caseData,
                        caseData.getSameRateInterestSelection().getDifferentRate(), interestToDate);
                }
            } else if (caseData.getInterestClaimOptions().name().equals(BREAK_DOWN_INTEREST)) {
                interestAmount = caseData.getBreakDownInterestTotal();
            }
        }
        return interestAmount;
    }

    public BigDecimal calculateInterestAmount(CaseData caseData, BigDecimal interestRate, LocalDate interestToDate) {
        if (caseData.getInterestClaimFrom().name().equals(FROM_CLAIM_SUBMIT_DATE)) {
            LocalDate fromDate = isAfterFourPM(caseData.getSubmittedDate()) ? caseData.getSubmittedDate().toLocalDate().plusDays(1) :
                caseData.getSubmittedDate().toLocalDate();
            return calculateInterestByDate(caseData.getTotalClaimAmount(), interestRate, fromDate, interestToDate);
        } else if (caseData.getInterestClaimFrom().name().equals(FROM_SPECIFIC_DATE)) {
            return calculateInterestByDate(caseData.getTotalClaimAmount(), interestRate,
                caseData.getInterestFromSpecificDate(), interestToDate);
        }
        return ZERO;
    }

    private LocalDate getToDate(CaseData caseData) {
        if (caseData.getInterestClaimUntil() != null && caseData.getInterestClaimUntil().name().equals(UNTIL_CLAIM_SUBMIT_DATE)) {
            return caseData.getSubmittedDate().toLocalDate();
        }
        return LocalDate.now();
    }

    protected BigDecimal calculateInterestByDate(BigDecimal claimAmount, BigDecimal interestRate, LocalDate
        interestFromSpecificDate, LocalDate interestToSpecificDate) {
        if (interestToSpecificDate == null) {
            interestToSpecificDate = time.now().toLocalDate();
        }
        long numberOfDays
            = Math.abs(ChronoUnit.DAYS.between(interestToSpecificDate, interestFromSpecificDate));
        BigDecimal interestPerDay = getInterestPerDay(claimAmount, interestRate);
        return interestPerDay.multiply(BigDecimal.valueOf(numberOfDays));
    }

    @NotNull
    private static BigDecimal getInterestPerDay(BigDecimal claimAmount, BigDecimal interestRate) {
        BigDecimal interestForAYear
            = claimAmount.multiply(interestRate.divide(HUNDRED));
        BigDecimal interestPerDay = interestForAYear.divide(NUMBER_OF_DAYS_IN_YEAR, TO_FULL_PENNIES,
            RoundingMode.HALF_UP);
        return interestPerDay;
    }

    public BigDecimal calculateBulkInterest(CaseData caseData) {
        if (caseData.getClaimInterest() == YesOrNo.YES) {
            long numberOfDays = Math.abs(ChronoUnit.DAYS.between(time.now().toLocalDate(), caseData.getInterestFromSpecificDate()));
            if (isAfterFourPM()) {
                numberOfDays = Math.abs(ChronoUnit.DAYS.between(time.now().toLocalDate(), caseData.getInterestFromSpecificDate().plusDays(1)));
            }
            BigDecimal interestDailyAmount = caseData.getSameRateInterestSelection().getDifferentRate();
            return interestDailyAmount.multiply(BigDecimal.valueOf(numberOfDays));
        } else {
            return ZERO;
        }
    }

    public String getInterestPerDayBreakdown(CaseData caseData) {
        if (caseData.getInterestClaimOptions().name().equals(BREAK_DOWN_INTEREST)) {
            return null;
        }
        StringBuilder description = new StringBuilder("Interest will accrue at the daily rate of £");
        BigDecimal interestPerDay = ZERO;
        if (caseData.getInterestClaimOptions().name().equals(SAME_RATE_INTEREST)) {
            if (caseData.getSameRateInterestSelection().getSameRateInterestType().name()
                .equals(SAME_RATE_INTEREST_8_PC)) {
                interestPerDay = getInterestPerDay(caseData.getTotalClaimAmount(), EIGHT_PERCENT_INTEREST_RATE);
            }
            if (caseData.getSameRateInterestSelection().getSameRateInterestType().name()
                .equals(SAME_RATE_INTEREST_DIFFERENT_RATE)) {
                interestPerDay = getInterestPerDay(caseData.getTotalClaimAmount(), caseData.getSameRateInterestSelection().getDifferentRate());
            }
        }
        description.append(interestPerDay.setScale(2, RoundingMode.HALF_UP));
        description.append(" up to the date of ");
        description.append(caseData.getInterestClaimUntil().name().equals(UNTIL_CLAIM_SUBMIT_DATE) ? "claim submission" : "judgement");
        return description.toString();
    }

    private boolean isAfterFourPM() {
        LocalTime localTime = time.now().toLocalTime();
        return localTime.getHour() > 15;
    }

    private boolean isAfterFourPM(LocalDateTime localDateTime) {
        return localDateTime.getHour() > 15;
    }
}
