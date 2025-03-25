package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static uk.gov.hmcts.reform.civil.utils.MonetaryConversions.HUNDRED;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterestCalculator {

    public static final int TO_FULL_PENNIES = 2;
    protected static final BigDecimal EIGHT_PERCENT_INTEREST_RATE = valueOf(8);
    public static final BigDecimal NUMBER_OF_DAYS_IN_YEAR = new BigDecimal(365L);

    public BigDecimal calculateInterest(CaseData caseData) {
        log.info("calculateInterest: {}", getToDate(caseData));
        return this.calculateInterest(caseData, getToDate(caseData));
    }

    private BigDecimal calculateInterest(CaseData caseData, LocalDate interestToDate) {
        BigDecimal interestAmount = ZERO;
        log.info("Claim interest: {}", caseData.getClaimInterest());
        if (caseData.getClaimInterest() == YesOrNo.YES) {
            log.info("InterestClaimOption: {}", caseData.getInterestClaimOptions());
            if (caseData.getInterestClaimOptions().equals(InterestClaimOptions.SAME_RATE_INTEREST)) {
                log.info("Rate interest selection: {}", caseData.getSameRateInterestSelection().getSameRateInterestType());
                if (caseData.getSameRateInterestSelection().getSameRateInterestType()
                    .equals(SameRateInterestType.SAME_RATE_INTEREST_8_PC)) {
                    interestAmount = calculateInterestAmount(caseData, EIGHT_PERCENT_INTEREST_RATE, interestToDate);
                    log.info("interest amount: {}", interestAmount);
                }
                if (caseData.getSameRateInterestSelection().getSameRateInterestType()
                    .equals(SameRateInterestType.SAME_RATE_INTEREST_DIFFERENT_RATE)) {
                    interestAmount = calculateInterestAmount(caseData,
                        caseData.getSameRateInterestSelection().getDifferentRate(), interestToDate);
                    log.info("interest amount2: {}", interestAmount);
                }
            } else if (caseData.getInterestClaimOptions().equals(InterestClaimOptions.BREAK_DOWN_INTEREST)) {
                interestAmount = caseData.getBreakDownInterestTotal();
                log.info("interest amount3: {}", interestAmount);
            }
        }
        return interestAmount;
    }

    private BigDecimal calculateInterestAmount(CaseData caseData, BigDecimal interestRate, LocalDate interestToDate) {
        log.info("caseData.getInterestClaimFrom(): {}", caseData.getInterestClaimFrom());
        if (caseData.getInterestClaimFrom().equals(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)) {
            LocalDate interestFromDate = getSubmittedDate(caseData);
            log.info("interestFromDate: {}", interestFromDate);
            log.info("interestToDate: {}", interestToDate);
            return calculateInterestByDate(caseData.getTotalClaimAmount(), interestRate, interestFromDate, interestToDate);
        } else if (caseData.getInterestClaimFrom().equals(InterestClaimFromType.FROM_A_SPECIFIC_DATE)) {
            log.info("interestRate: {}", interestRate);
            log.info("interestToDate: {}", interestToDate);
            log.info("TotalClaimAmount: {}", caseData.getTotalClaimAmount());
            log.info("getInterestFromSpecificDate: {}", caseData.getInterestFromSpecificDate());
            return calculateInterestByDate(caseData.getTotalClaimAmount(), interestRate,
                caseData.getInterestFromSpecificDate(), interestToDate);
        }
        return ZERO;
    }

    private LocalDate getToDate(CaseData caseData) {
        if (Objects.nonNull(caseData.getInterestClaimUntil())
            && caseData.getInterestClaimUntil().equals(InterestClaimUntilType.UNTIL_CLAIM_SUBMIT_DATE)) {
            return getSubmittedDate(caseData);
        }
        return LocalDate.now();
    }

    protected BigDecimal calculateInterestByDate(BigDecimal claimAmount, BigDecimal interestRate, LocalDate
        interestFromSpecificDate, LocalDate interestToSpecificDate) {
        long numberOfDays = getNumberOfDays(interestFromSpecificDate, interestToSpecificDate);
        BigDecimal interestPerDay = getInterestPerDay(claimAmount, interestRate);
        log.info("numberOfDays: {}", numberOfDays);
        log.info("interestPerDay: {}", interestPerDay);
        return interestPerDay.multiply(BigDecimal.valueOf(numberOfDays));
    }

    private static long getNumberOfDays(LocalDate interestFromSpecificDate, LocalDate interestToSpecificDate) {
        long numberOfDays
            = Math.abs(ChronoUnit.DAYS.between(interestToSpecificDate, interestFromSpecificDate));
        return numberOfDays;
    }

    @NotNull
    private static BigDecimal getInterestPerDay(BigDecimal claimAmount, BigDecimal interestRate) {
        BigDecimal interestForAYear
            = claimAmount.multiply(interestRate.divide(HUNDRED));
        log.info("InterestForAYear: {}", interestForAYear);
        BigDecimal interestPerDay = interestForAYear.divide(NUMBER_OF_DAYS_IN_YEAR, TO_FULL_PENNIES,
            RoundingMode.HALF_UP);
        log.info("interestPerDay: {}", interestPerDay);
        return interestPerDay;
    }

    public BigDecimal calculateBulkInterest(CaseData caseData) {
        if (caseData.getClaimInterest() == YesOrNo.YES) {
            long numberOfDays = getNumberOfDays(caseData.getInterestFromSpecificDate(), LocalDate.now());
            BigDecimal interestDailyAmount = caseData.getSameRateInterestSelection().getDifferentRate();
            return interestDailyAmount.multiply(BigDecimal.valueOf(numberOfDays));
        } else {
            return ZERO;
        }
    }

    public String getInterestPerDayBreakdown(CaseData caseData) {

        if (caseData.getInterestClaimUntil() != null && caseData.getInterestClaimUntil().equals(InterestClaimUntilType.UNTIL_CLAIM_SUBMIT_DATE)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            return caseData.getSubmittedDate().toLocalDate().format(formatter);
        } else if (caseData.getInterestClaimOptions() == null
            || caseData.getInterestClaimOptions().equals(InterestClaimOptions.BREAK_DOWN_INTEREST)) {
            return null;
        } else {
            StringBuilder description = new StringBuilder("Interest will accrue at the daily rate of £");
            BigDecimal interestPerDay = ZERO;
            if (caseData.getInterestClaimOptions().equals(InterestClaimOptions.SAME_RATE_INTEREST)) {
                if (caseData.getSameRateInterestSelection().getSameRateInterestType()
                    .equals(SameRateInterestType.SAME_RATE_INTEREST_8_PC)) {
                    interestPerDay = getInterestPerDay(caseData.getTotalClaimAmount(), EIGHT_PERCENT_INTEREST_RATE);
                }
                if (caseData.getSameRateInterestSelection().getSameRateInterestType()
                    .equals(SameRateInterestType.SAME_RATE_INTEREST_DIFFERENT_RATE)) {
                    interestPerDay = getInterestPerDay(caseData.getTotalClaimAmount(), caseData.getSameRateInterestSelection().getDifferentRate());
                }
            }
            description.append(interestPerDay.setScale(2, RoundingMode.HALF_UP));
            description.append(" up to the date of judgment or settlement");
            return description.toString();
        }
    }

    private LocalDate getSubmittedDate(CaseData caseData) {
        return Objects.nonNull(caseData.getSubmittedDate()) ? caseData.getSubmittedDate().toLocalDate() : LocalDate.now();
    }

}
