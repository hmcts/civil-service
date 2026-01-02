package uk.gov.hmcts.reform.civil.helpers;

import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.ClaimTypeUnspec;

public class ClaimTypeHelper {

    private ClaimTypeHelper() {
        // Utility class, no instances
    }

    public static ClaimType getClaimTypeFromClaimTypeUnspec(ClaimTypeUnspec claimTypeUnspec) {
        return switch (claimTypeUnspec) {
            case PERSONAL_INJURY -> ClaimType.PERSONAL_INJURY;
            case CLINICAL_NEGLIGENCE -> ClaimType.CLINICAL_NEGLIGENCE;
            case PROFESSIONAL_NEGLIGENCE -> ClaimType.PROFESSIONAL_NEGLIGENCE;
            case BREACH_OF_CONTRACT -> ClaimType.BREACH_OF_CONTRACT;
            case CONSUMER -> ClaimType.CONSUMER;
            case CONSUMER_CREDIT -> ClaimType.CONSUMER_CREDIT;
            case DAMAGES_AND_OTHER_REMEDY -> ClaimType.DAMAGES_AND_OTHER_REMEDY;
            case HOUSING_DISREPAIR -> ClaimType.HOUSING_DISREPAIR;
            case OTHER -> ClaimType.OTHER;
            default -> throw new IllegalArgumentException("Invalid Claim Type");
        };
    }
}
