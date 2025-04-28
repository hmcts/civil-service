package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class CaseStateUtils {

    private CaseStateUtils() {
        //NO-OP
    }

    public static boolean shouldMoveToInMediationState(CaseData caseData, boolean carmEnabled) {
        if (carmEnabled && SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack())) {
            return caseData.hasApplicantProceededWithClaim()
                || (caseData.getCaseDataLiP() != null
                && NO.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim()));
        }
        return false;
    }
}
