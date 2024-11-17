package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;

public class DefaultJudgmentUtils {

    private static final int COMMENCEMENT_FIXED_COST_60 = 60;
    private static final int COMMENCEMENT_FIXED_COST_80 = 80;
    private static final int COMMENCEMENT_FIXED_COST_90 = 90;
    private static final int COMMENCEMENT_FIXED_COST_110 = 110;
    private static final int ENTRY_FIXED_COST_22 = 22;
    private static final int ENTRY_FIXED_COST_30 = 30;

    private static final BigDecimal JUDGMENT_AMOUNT_5000 = BigDecimal.valueOf(5000);
    private static final BigDecimal JUDGMENT_AMOUNT_25 = BigDecimal.valueOf(25);

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

    public static BigDecimal calculateFixedCostsOnEntry(CaseData caseData, BigDecimal judgmentAmount) {
        BigDecimal claimIssueFixedCost = MonetaryConversions.penniesToPounds(BigDecimal.valueOf(
            Integer.parseInt(caseData.getFixedCosts().getFixedCostAmount())));
        if (YesOrNo.YES.equals(caseData.getClaimFixedCostsOnEntryDJ())) {
            if (judgmentAmount.compareTo(JUDGMENT_AMOUNT_5000) > 0) {
                return claimIssueFixedCost.add(BigDecimal.valueOf(ENTRY_FIXED_COST_30));
            } else if (judgmentAmount.compareTo(JUDGMENT_AMOUNT_25) > 0) {
                return claimIssueFixedCost.add(BigDecimal.valueOf(ENTRY_FIXED_COST_22));
            }
        }
        return claimIssueFixedCost;
    }
}
