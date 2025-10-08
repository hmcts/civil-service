package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import java.math.BigDecimal;

import static uk.gov.hmcts.reform.civil.utils.MonetaryConversions.penniesToPounds;

public class ClaimFeeUtility {

    private ClaimFeeUtility() {
        // Utility class
    }

    public static BigDecimal getCourtFee(CaseData caseData) {
        if (caseData.getClaimFee() == null) {
            return null;
        }

        HelpWithFeesDetails hwfDetails = caseData.getClaimIssuedHwfDetails();
        BigDecimal calculatedFee = penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence());

        if (hwfDetails != null) {
            if (hwfDetails.getRemissionAmount() != null
                && hwfDetails.getRemissionAmount().compareTo(caseData.getClaimFee().getCalculatedAmountInPence()) == 0) {
                return BigDecimal.ZERO;
            }

            if (hwfDetails.getOutstandingFeeInPounds() != null) {
                return hwfDetails.getOutstandingFeeInPounds();
            }
        }
        return calculatedFee;
    }
}
