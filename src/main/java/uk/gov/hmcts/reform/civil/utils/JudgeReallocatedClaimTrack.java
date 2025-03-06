package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class JudgeReallocatedClaimTrack {

    private JudgeReallocatedClaimTrack() {
        //No op
    }

    public static boolean judgeReallocatedTrackOrAlreadyMinti(CaseData caseData, Boolean multiOrIntermediateTrackEnabled) {
        boolean isClaimReallocated = caseData.getFinalOrderAllocateToTrack() != null && caseData.getFinalOrderAllocateToTrack().equals(YES);
        return isClaimReallocated || isCurrentTrackMinti(caseData, multiOrIntermediateTrackEnabled);
    }

    private static boolean isCurrentTrackMinti(CaseData caseData, boolean multiOrIntermediateTrackEnabled) {
        if (!multiOrIntermediateTrackEnabled) {
            System.out.println("SHOULD BE TRUE");
            return false;
        }

        return AllocatedTrack.INTERMEDIATE_CLAIM.equals(caseData.getAllocatedTrack())
                || AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
                || AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())
                || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack());
    }

}
