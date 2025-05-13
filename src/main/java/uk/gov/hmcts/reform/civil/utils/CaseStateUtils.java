package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class CaseStateUtils {

    private CaseStateUtils() {
        //NO-OP
    }

    public static boolean shouldMoveToInMediationState(CaseData caseData, FeatureToggleService featureToggleService) {
        if (caseData.isFullAdmitClaimSpec()) {
            return false;
        }
        if (featureToggleService.isCarmEnabledForCase(caseData) && SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack())) {
            return caseData.hasApplicantProceededWithClaim()
                || (caseData.getCaseDataLiP() != null
                && NO.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim()));
        }
        return caseData.hasDefendantAgreedToFreeMediation() && caseData.hasClaimantAgreedToFreeMediation();
    }
}
