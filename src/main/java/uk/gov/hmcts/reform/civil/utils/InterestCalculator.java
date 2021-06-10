package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;

public class InterestCalculator {

    private BigDecimal interest;

    public BigDecimal calculateInterest(CaseData caseData) {

        if(caseData.getClaimInterest() == YesOrNo.YES) {
            if(caseData.getInterestClaimOptions().name() == "SAME_RATE_INTEREST") {
                if(caseData.getSameRateInterestSelection().getSameRateInterestType().name() == "SAME_RATE_INTEREST_8_PC") {
                   return interest = calculateInterestAmount(caseData, new BigDecimal(8));
                }
                if(caseData.getSameRateInterestSelection().getSameRateInterestType().name() == "SAME_RATE_INTEREST_DIFFERENT_RATE") {
                    return interest = calculateInterestAmount(caseData, caseData.getSameRateInterestSelection().getADifferentRate());
                }
            }
            else if(caseData.getInterestClaimOptions().name() == "BREAK_DOWN_INTEREST") {
                return  interest = caseData.getBreakDownInterestTotal();
            }
        } else {
            interest = new BigDecimal(0.00);
        }
        return interest;
    }

    private BigDecimal calculateInterestAmount(CaseData caseData, BigDecimal interestRate) {


        if(caseData.getInterestClaimFrom().name() == "FROM_CLAIM_SUBMIT_DATE") {
            return  interest = new BigDecimal(0.00); //change this
        }

        if(caseData.getInterestClaimFrom().name() == "FROM_A_SPECIFIC_DATE") {
            //logic for specific date
            if(caseData.getInterestClaimUntil().name() == "UNTIL_CLAIM_SUBMIT_DATE" || caseData.getInterestClaimUntil().name() == "UNTIL_SETTLED_OR_JUDGEMENT_MADE" ) {
                return interest = caseData.getTotalClaimAmount().multiply(interestRate.divide(new BigDecimal(100)));
            }
        }
        return interest;
    }
}
