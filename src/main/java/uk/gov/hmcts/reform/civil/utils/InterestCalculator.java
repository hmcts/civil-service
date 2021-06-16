package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class InterestCalculator {

    private BigDecimal interest;

    public BigDecimal calculateInterest(CaseData caseData) {

        if (caseData.getClaimInterest() == YesOrNo.YES) {
            if (caseData.getInterestClaimOptions().name() == "SAME_RATE_INTEREST") {
                if (caseData.getSameRateInterestSelection().getSameRateInterestType().name()
                    == "SAME_RATE_INTEREST_8_PC") {
                    return calculateInterestAmount(caseData, new BigDecimal(8));
                }
                if (caseData.getSameRateInterestSelection().getSameRateInterestType().name()
                    == "SAME_RATE_INTEREST_DIFFERENT_RATE") {
                    return calculateInterestAmount(caseData,
                                                   caseData.getSameRateInterestSelection().getDifferentRate());
                }
            } else if (caseData.getInterestClaimOptions().name() == "BREAK_DOWN_INTEREST") {
                return  caseData.getBreakDownInterestTotal();
            }
        } else {
            return new BigDecimal(0.00);
        }
        return new BigDecimal(0.00);
    }

    private BigDecimal calculateInterestAmount(CaseData caseData, BigDecimal interestRate) {

        if (caseData.getInterestClaimFrom().name() == "FROM_CLAIM_SUBMIT_DATE") {
            return new BigDecimal(0.00); //change this
        }

        if (caseData.getInterestClaimFrom().name() == "FROM_A_SPECIFIC_DATE") {
            //logic for specific date, calculate number of days here
            if (caseData.getInterestClaimUntil().name() == "UNTIL_CLAIM_SUBMIT_DATE"
                || caseData.getInterestClaimUntil().name() == "UNTIL_SETTLED_OR_JUDGEMENT_MADE") {

                BigDecimal numberOfDays
                    = new BigDecimal(calculateDaysBetweenDates(caseData.getInterestFromSpecificDate()));
                BigDecimal interestForAYear
                    = caseData.getTotalClaimAmount().multiply(interestRate.divide(new BigDecimal(100)));
                BigDecimal  interestPerDay = interestForAYear.divide(new BigDecimal(365.00), 2, RoundingMode.HALF_UP);
                return interestPerDay.multiply(numberOfDays);
            }
        }
        return new BigDecimal(0.00);
    }

    public long calculateDaysBetweenDates(LocalDate specificDate) {

        Date today = new Date();
        long diff =
            today.getTime() - Date.from(specificDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime();
        long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        return days;
    }
}


