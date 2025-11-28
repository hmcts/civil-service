package uk.gov.hmcts.reform.civil.utils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
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

@Component
@NoArgsConstructor
@Slf4j
public class InterestCalculator {

    public static final int TO_FULL_PENNIES = 2;
    protected static final BigDecimal EIGHT_PERCENT_INTEREST_RATE = valueOf(8);
    public static final BigDecimal NUMBER_OF_DAYS_IN_YEAR = new BigDecimal(365L);

    public BigDecimal calculateInterest(CaseData caseData) {
        return this.calculateInterest(caseData, getToDate(caseData));
    }

    private BigDecimal calculateInterest(CaseData caseData, LocalDate interestToDate) {
        log.info("Calculating interest for case id: {}", caseData.getCcdCaseReference());
        BigDecimal interestAmount = ZERO;
        if (!validateInterestCalculationRequestData(caseData)) {
            log.error("Interest calculation request data is invalid for case id: {}", caseData.getCcdCaseReference());
            return interestAmount;
        } else  if (caseData.getClaimInterest() == YesOrNo.YES) {
            if (InterestClaimOptions.SAME_RATE_INTEREST.equals(caseData.getInterestClaimOptions())) {
                SameRateInterestSelection sameRateInterestSelection = caseData.getSameRateInterestSelection();
                if (sameRateInterestSelection != null && SameRateInterestType.SAME_RATE_INTEREST_8_PC
                    .equals(sameRateInterestSelection.getSameRateInterestType())) {
                    interestAmount = calculateInterestAmount(caseData, EIGHT_PERCENT_INTEREST_RATE, interestToDate);
                } else if (sameRateInterestSelection != null && SameRateInterestType.SAME_RATE_INTEREST_DIFFERENT_RATE
                    .equals(sameRateInterestSelection.getSameRateInterestType())) {
                    interestAmount = calculateInterestAmount(caseData,
                                                             sameRateInterestSelection.getDifferentRate(), interestToDate);
                } else {
                    log.error("No same rate interest type selected for case id: {}", caseData.getCcdCaseReference());
                }
            } else if (InterestClaimOptions.BREAK_DOWN_INTEREST.equals(caseData.getInterestClaimOptions())) {
                interestAmount = caseData.getBreakDownInterestTotal();
            } else {
                log.error("No interest claim options selected for case id: {}", caseData.getCcdCaseReference());
            }
        } else {
            log.info("Interest not calculated for case id: {}, claim interest is no", caseData.getCcdCaseReference());
            return interestAmount;
        }
        log.info("Interest calculated for case id: {}, amount: {}", caseData.getCcdCaseReference(), interestAmount);
        return interestAmount;
    }

    private boolean validateInterestCalculationRequestData(CaseData caseData) {
        if (caseData.getClaimInterest() == YesOrNo.YES) {
            if (InterestClaimOptions.SAME_RATE_INTEREST.equals(caseData.getInterestClaimOptions())) {
                SameRateInterestSelection sameRateInterestSelection = caseData.getSameRateInterestSelection();
                if (sameRateInterestSelection == null) {
                    log.error(
                        "No same rate interest selection selected for case id: {}",
                        caseData.getCcdCaseReference()
                    );
                    return false;
                } else if (caseData.getInterestClaimFrom() == null) {
                    log.error("No interest claim from selected for case id: {}", caseData.getCcdCaseReference());
                    return false;
                } else if (caseData.getInterestClaimFrom() == InterestClaimFromType.FROM_A_SPECIFIC_DATE && caseData.getInterestFromSpecificDate() == null) {
                    log.error("No interest claim from date selected for case id: {}", caseData.getCcdCaseReference());
                    return false;
                }
            }
        }
        return true;
    }

    public BigDecimal claimAmountPlusInterestToDate(CaseData caseData) {
        return caseData.getTotalClaimAmount().add(calculateInterest(caseData));
    }

    private BigDecimal calculateInterestAmount(CaseData caseData, BigDecimal interestRate, LocalDate interestToDate) {
        if (InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE.equals(caseData.getInterestClaimFrom())) {
            LocalDate interestFromDate = getSubmittedDate(caseData);
            return calculateInterestByDate(caseData.getTotalClaimAmount(), interestRate, interestFromDate, interestToDate);
        } else if (InterestClaimFromType.FROM_A_SPECIFIC_DATE.equals(caseData.getInterestClaimFrom())) {
            return calculateInterestByDate(caseData.getTotalClaimAmount(), interestRate,
                caseData.getInterestFromSpecificDate(), interestToDate);
        }
        return ZERO;
    }

    private LocalDate getToDate(CaseData caseData) {
        if (Objects.nonNull(caseData.getInterestClaimUntil())
            && InterestClaimUntilType.UNTIL_CLAIM_SUBMIT_DATE.equals(caseData.getInterestClaimUntil())) {
            return getSubmittedDate(caseData);
        }
        return LocalDate.now();
    }

    protected BigDecimal calculateInterestByDate(BigDecimal claimAmount, BigDecimal interestRate, LocalDate
        interestFromSpecificDate, LocalDate interestToSpecificDate) {
        long numberOfDays = getNumberOfDays(interestFromSpecificDate, interestToSpecificDate);
        BigDecimal interestPerDay = getInterestPerDay(claimAmount, interestRate);
        return interestPerDay.multiply(BigDecimal.valueOf(numberOfDays));
    }

    private static long getNumberOfDays(LocalDate interestFromSpecificDate, LocalDate interestToSpecificDate) {
        long numberOfDays = 0;
        //Do not count number of days if they're negative, i.e. if the 'To' date is before the 'From' date
        if (interestToSpecificDate.isAfter(interestFromSpecificDate)) {
            numberOfDays = Math.abs(ChronoUnit.DAYS.between(interestToSpecificDate, interestFromSpecificDate));
        }
        return numberOfDays;
    }

    @NotNull
    private static BigDecimal getInterestPerDay(BigDecimal claimAmount, BigDecimal interestRate) {
        BigDecimal interestForAYear
            = claimAmount.multiply(interestRate.divide(HUNDRED));
        return interestForAYear.divide(NUMBER_OF_DAYS_IN_YEAR, TO_FULL_PENNIES,
            RoundingMode.HALF_UP);
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

        if (InterestClaimUntilType.UNTIL_CLAIM_SUBMIT_DATE.equals(caseData.getInterestClaimUntil())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            return caseData.getSubmittedDate().toLocalDate().format(formatter);
        } else if (caseData.getInterestClaimOptions() == null
            || InterestClaimOptions.BREAK_DOWN_INTEREST.equals(caseData.getInterestClaimOptions())) {
            return null;
        } else {
            StringBuilder description = new StringBuilder("Interest will accrue at the daily rate of £");
            BigDecimal interestPerDay = ZERO;
            if (InterestClaimOptions.SAME_RATE_INTEREST.equals(caseData.getInterestClaimOptions())) {
                if (SameRateInterestType.SAME_RATE_INTEREST_8_PC
                    .equals(caseData.getSameRateInterestSelection().getSameRateInterestType())) {
                    interestPerDay = getInterestPerDay(caseData.getTotalClaimAmount(), EIGHT_PERCENT_INTEREST_RATE);
                }
                if (SameRateInterestType.SAME_RATE_INTEREST_DIFFERENT_RATE
                    .equals(caseData.getSameRateInterestSelection().getSameRateInterestType())) {
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
