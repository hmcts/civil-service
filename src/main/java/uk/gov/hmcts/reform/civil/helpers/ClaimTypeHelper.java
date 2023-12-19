package uk.gov.hmcts.reform.civil.helpers;

import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.model.CaseData;

public class ClaimTypeHelper {

    private ClaimTypeHelper() {
        // Utility class, no instances
    }

    public static ClaimType copyClaimTypeUnspecToClaimType(CaseData caseData) {
        switch (caseData.getClaimTypeUnSpec()) {
            case PERSONAL_INJURY:
                return ClaimType.PERSONAL_INJURY;
            case CLINICAL_NEGLIGENCE:
                return ClaimType.CLINICAL_NEGLIGENCE;
            case PROFESSIONAL_NEGLIGENCE:
                return ClaimType.PROFESSIONAL_NEGLIGENCE;
            case BREACH_OF_CONTRACT:
                return ClaimType.BREACH_OF_CONTRACT;
            case CONSUMER:
                return ClaimType.CONSUMER;
            case CONSUMER_CREDIT:
                return ClaimType.CONSUMER_CREDIT;
            case OTHER:
                return ClaimType.OTHER;
            default:
                throw new IllegalArgumentException("Invalid Claim Type");
        }
    }
}
