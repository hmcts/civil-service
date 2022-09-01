package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;

public class DefaultJudgmentUtils {

    private static final int COMMENCEMENT_FIXED_COST_60 = 60;
    private static final int COMMENCEMENT_FIXED_COST_80 = 80;
    private static final int COMMENCEMENT_FIXED_COST_90 = 90;
    private static final int COMMENCEMENT_FIXED_COST_110 = 110;
    private static final int ENTRY_FIXED_COST_22 = 22;
    private static final int ENTRY_FIXED_COST_30 = 30;

    private DefaultJudgmentUtils() {

    }

    public static BigDecimal calculateFixedCosts(CaseData caseData) {

        int fixedCost = 0;
        int totalClaimAmount = caseData.getTotalClaimAmount().intValue();
        if (totalClaimAmount > 25 && totalClaimAmount <= 5000) {
            if (totalClaimAmount <= 500) {
                fixedCost = COMMENCEMENT_FIXED_COST_60;
            } else if (totalClaimAmount <= 1000) {
                fixedCost = COMMENCEMENT_FIXED_COST_80;
            } else {
                fixedCost = COMMENCEMENT_FIXED_COST_90;
            }
            fixedCost = fixedCost + ENTRY_FIXED_COST_22;
        } else if (totalClaimAmount > 5000) {
            fixedCost = COMMENCEMENT_FIXED_COST_110 + ENTRY_FIXED_COST_30;
        }
        return new BigDecimal(fixedCost);
    }
}
