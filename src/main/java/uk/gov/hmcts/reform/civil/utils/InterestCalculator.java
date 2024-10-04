package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
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
import java.util.Objects;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.isAfterFourPM;
import static  uk.gov.hmcts.reform.civil.utils.MonetaryConversions.HUNDRED;

@Component
@RequiredArgsConstructor
public class InterestCalculator {

    public static final int TO_FULL_PENNIES = 2;
    private static final String FROM_CLAIM_SUBMIT_DATE = "FROM_CLAIM_SUBMIT_DATE";
    private static final String FROM_SPECIFIC_DATE = "FROM_A_SPECIFIC_DATE";
    private static final String UNTIL_CLAIM_SUBMIT_DATE = "UNTIL_CLAIM_SUBMIT_DATE";
    private static final String UNTIL_SETTLED_OR_JUDGEMENT_MADE = "UNTIL_SETTLED_OR_JUDGEMENT_MADE";
    public static final BigDecimal NUMBER_OF_DAYS_IN_YEAR = new BigDecimal(365L);
    public LocalDateTime localDateTime = LocalDateTime.now();
    private final Time time;

    public BigDecimal calculateInterest(CaseData caseData) {
        BigDecimal interestAmount = ZERO;
        if (caseData.getClaimInterest() == YesOrNo.YES) {
            if (caseData.getInterestClaimOptions().name().equals("SAME_RATE_INTEREST")) {
                if (caseData.getSameRateInterestSelection().getSameRateInterestType().name()
                    .equals("SAME_RATE_INTEREST_8_PC")) {
                    interestAmount = calculateInterestAmount(caseData, valueOf(8));
                }
                if (caseData.getSameRateInterestSelection().getSameRateInterestType().name()
                    .equals("SAME_RATE_INTEREST_DIFFERENT_RATE")) {
                    interestAmount = calculateInterestAmount(caseData,
                                                   caseData.getSameRateInterestSelection().getDifferentRate());
                }
            } else if (caseData.getInterestClaimOptions().name().equals("BREAK_DOWN_INTEREST")) {
                interestAmount =  caseData.getBreakDownInterestTotal();
            }
        }
        return interestAmount;
    }

    public BigDecimal calculateInterestAmount(CaseData caseData, BigDecimal interestRate) {
        LocalDate interestEndDate =  time.now().toLocalDate();

        if (caseData.getInterestClaimFrom().name().equals(FROM_CLAIM_SUBMIT_DATE)) {
            LocalDateTime claimSubmitDate = Objects.nonNull(caseData.getSubmittedDate()) ? caseData.getSubmittedDate() : time.now();
            LocalDate interestStartDate = isAfterFourPM(claimSubmitDate) ? claimSubmitDate.toLocalDate().plusDays(1) :
                claimSubmitDate.toLocalDate();
            interestEndDate = isAfter4PM() ? interestEndDate.plusDays(1) : interestEndDate;
            return calculateInterestByDate(caseData.getTotalClaimAmount(), interestRate, interestStartDate, interestEndDate);
        } else if (caseData.getInterestClaimFrom().name().equals(FROM_SPECIFIC_DATE)) {
            LocalDate interestStartDate = caseData.getInterestFromSpecificDate();

            if (caseData.getInterestClaimUntil().name().equals(UNTIL_CLAIM_SUBMIT_DATE)) {
                LocalDateTime endDate = Objects.nonNull(caseData.getSubmittedDate()) ? caseData.getSubmittedDate() : time.now();
                interestEndDate = isAfterFourPM(endDate) ? endDate.toLocalDate().plusDays(2) : endDate.toLocalDate().plusDays(1);
            } else {
                interestEndDate = isAfterFourPM(time.now()) ? time.now().toLocalDate().plusDays(2) : time.now().toLocalDate().plusDays(
                    1);
            }
            return calculateInterestByDate(caseData.getTotalClaimAmount(), interestRate,
                                           interestStartDate, interestEndDate);
        }
        return ZERO;
    }

    public BigDecimal calculateInterestByDate(BigDecimal claimAmount, BigDecimal interestRate, LocalDate
        interestStartDate, LocalDate interestEndDate) {
        long numberOfDays
            = Math.abs(ChronoUnit.DAYS.between(interestStartDate, interestEndDate));
        BigDecimal interestForAYear
            = claimAmount.multiply(interestRate.divide(HUNDRED));
        BigDecimal  interestPerDay = interestForAYear.divide(NUMBER_OF_DAYS_IN_YEAR, TO_FULL_PENNIES,
                                                             RoundingMode.HALF_UP);
        return interestPerDay.multiply(BigDecimal.valueOf(numberOfDays));
    }

    public BigDecimal calculateBulkInterest(CaseData caseData) {
        if (caseData.getClaimInterest() == YesOrNo.YES) {
            long numberOfDays = Math.abs(ChronoUnit.DAYS.between(time.now().toLocalDate(), caseData.getInterestFromSpecificDate()));
            if (isAfterFourPM(time.now())) {
                numberOfDays = Math.abs(ChronoUnit.DAYS.between(time.now().toLocalDate(), caseData.getInterestFromSpecificDate().plusDays(1)));
            }
            BigDecimal interestDailyAmount = caseData.getSameRateInterestSelection().getDifferentRate();
            return interestDailyAmount.multiply(BigDecimal.valueOf(numberOfDays));
        } else {
            return ZERO;
        }
    }

    private boolean isAfter4PM() {
        LocalTime localTime = time.now().toLocalTime();
        return localTime.getHour() > 15;
    }
}
