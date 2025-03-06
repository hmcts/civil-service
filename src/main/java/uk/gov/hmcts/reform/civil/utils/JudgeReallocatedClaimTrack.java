package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class JudgeReallocatedClaimTrack {

    private JudgeReallocatedClaimTrack() {
        //No op
    }

    public static Boolean hasJudgeReallocatedTrack(CaseData caseData) {
        return caseData.getFinalOrderAllocateToTrack() != null && caseData.getFinalOrderAllocateToTrack().equals(YES);
    }

}
