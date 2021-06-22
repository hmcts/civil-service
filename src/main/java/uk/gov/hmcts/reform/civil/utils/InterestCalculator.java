package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static  uk.gov.hmcts.reform.civil.utils.MonetaryConversions.HUNDRED;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;

public class InterestCalculator {

    public static final int TO_FULL_PENNIES = 2;
    private static final String FROM_CLAIM_SUBMIT_DATE = "FROM_CLAIM_SUBMIT_DATE";
    private static final String FROM_SPECIFIC_DATE = "FROM_A_SPECIFIC_DATE";
    private static final String UNTIL_CLAIM_SUBMIT_DATE = "UNTIL_CLAIM_SUBMIT_DATE";
    private static final String UNTIL_SETTLED_OR_JUDGEMENT_MADE = "UNTIL_SETTLED_OR_JUDGEMENT_MADE";
    public static final BigDecimal NUMBER_OF_DAYS_IN_YEAR = new BigDecimal(365L);
    public static LocalDateTime localDateTime = LocalDateTime.now();

    public static BigDecimal calculateInterest(CaseData caseData) {
        BigDecimal interestAmount = ZERO;
        if (caseData.getClaimInterest() == YesOrNo.YES) {
            if (caseData.getInterestClaimOptions().name() == "SAME_RATE_INTEREST") {
                if (caseData.getSameRateInterestSelection().getSameRateInterestType().name()
                    == "SAME_RATE_INTEREST_8_PC") {
                    interestAmount = calculateInterestAmount(caseData, valueOf(8));
                }
                if (caseData.getSameRateInterestSelection().getSameRateInterestType().name()
                    == "SAME_RATE_INTEREST_DIFFERENT_RATE") {
                    interestAmount = calculateInterestAmount(caseData,
                                                   caseData.getSameRateInterestSelection().getDifferentRate());
                }
            } else if (caseData.getInterestClaimOptions().name() == "BREAK_DOWN_INTEREST") {
                interestAmount =  caseData.getBreakDownInterestTotal();
            }
        }
        return interestAmount;
    }

    public static BigDecimal calculateInterestAmount(CaseData caseData, BigDecimal interestRate) {
        if (caseData.getInterestClaimFrom().name() == FROM_CLAIM_SUBMIT_DATE) {
            LocalDate claimIssueDate = isAfterFourPM() ? localDateTime.toLocalDate().plusDays(1) :
                localDateTime.toLocalDate();
            return calculateInterestByDate(caseData.getTotalClaimAmount(), interestRate, claimIssueDate);
        } else if (caseData.getInterestClaimFrom().name() == FROM_SPECIFIC_DATE) {
            if (caseData.getInterestClaimUntil().name() == UNTIL_CLAIM_SUBMIT_DATE
                || caseData.getInterestClaimUntil().name() == UNTIL_SETTLED_OR_JUDGEMENT_MADE) {
                LocalDate claimIssueDate = isAfterFourPM() ?
                    caseData.getInterestFromSpecificDate().minusDays(1) :
                    caseData.getInterestFromSpecificDate();
                return calculateInterestByDate(caseData.getTotalClaimAmount(), interestRate,
                                         claimIssueDate);
            }
        }
        return ZERO;
    }

    public static BigDecimal calculateInterestByDate(BigDecimal claimAmount, BigDecimal interestRate, LocalDate
        InterestFromSpecificDate) {
        long numberOfDays
            = Math.abs(ChronoUnit.DAYS.between(localDateTime.toLocalDate(), InterestFromSpecificDate));
        BigDecimal interestForAYear
            = claimAmount.multiply(interestRate.divide(HUNDRED));
        BigDecimal  interestPerDay = interestForAYear.divide(NUMBER_OF_DAYS_IN_YEAR, TO_FULL_PENNIES,
                                                             RoundingMode.HALF_UP);
        return interestPerDay.multiply(BigDecimal.valueOf(numberOfDays));
    }

    private static boolean isAfterFourPM() {
        LocalTime localTime = localDateTime.toLocalTime();
        return localTime.getHour() > 15;
    }
}
