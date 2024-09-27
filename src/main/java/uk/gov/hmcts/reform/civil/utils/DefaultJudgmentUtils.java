package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;

public class DefaultJudgmentUtils {

    //the value of the claim exceeds £25 but does not exceed £500 = £50
    //the value of the claim exceeds £500 but does not exceed £1,000 = £70
    //the value of the claim exceeds £1,000 but does not exceed £5,000; or the only claim is for delivery of goods and no value is specified or stated on the claim form = £80
    //the value of the claim exceeds £5,000 = £100

    private static final int COMMENCEMENT_FIXED_COST_50 = 50;
    private static final int COMMENCEMENT_FIXED_COST_70 = 70;
    private static final int COMMENCEMENT_FIXED_COST_80 = 80;
    private static final int COMMENCEMENT_FIXED_COST_100 = 100;
    private static final int ENTRY_FIXED_COST_22 = 22;
    private static final int ENTRY_FIXED_COST_30 = 30;

    private DefaultJudgmentUtils() {

    }

    public static BigDecimal calculateFixedCosts(CaseData caseData) {

        int fixedCost = 0;
        int totalClaimAmount = caseData.getTotalClaimAmount().intValue();
        if (totalClaimAmount > 25 && totalClaimAmount <= 5000) {
            if (totalClaimAmount <= 500) {
                fixedCost = COMMENCEMENT_FIXED_COST_50;
            } else if (totalClaimAmount <= 1000) {
                fixedCost = COMMENCEMENT_FIXED_COST_70;
            } else {
                fixedCost = COMMENCEMENT_FIXED_COST_80;
            }
            fixedCost = fixedCost + ENTRY_FIXED_COST_22;
        } else if (totalClaimAmount > 5000) {
            fixedCost = COMMENCEMENT_FIXED_COST_100 + ENTRY_FIXED_COST_30;
        }
        return new BigDecimal(fixedCost);
    }
}
