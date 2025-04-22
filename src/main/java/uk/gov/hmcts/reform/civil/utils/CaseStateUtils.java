package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class CaseStateUtils {

    private CaseStateUtils() {
        //NO-OP
    }

    public static boolean shouldMoveToInMediationState(CaseData caseData, boolean carmEnabled) {
        if (carmEnabled && SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack())) {
            return YES.equals(caseData.getApplicant1ProceedWithClaim())
                || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())
                || (caseData.getCaseDataLiP() != null
                && NO.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim()));
        }
        return false;
    }
}
