package uk.gov.hmcts.reform.civil.helpers;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

public class SettleClaimHelper {

    private SettleClaimHelper() {
        // Utility class, no instances
    }

    public static void checkState(CaseData caseData, List<String> errors) {

        if (caseData.getCcdState().equals(CaseState.All_FINAL_ORDERS_ISSUED)) {
            errors.add("This action is not currently allowed at this stage");
        }

        if (caseData.isApplicantLiP()
            || MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)
            || (caseData.getAddRespondent2() == YesOrNo.YES && (caseData.isRespondent1LiP() || caseData.isRespondent2LiP()))
            || (caseData.getAddApplicant2() == YesOrNo.YES && caseData.getAddRespondent2() == YesOrNo.YES
                && (caseData.isRespondent1LiP() || caseData.isRespondent2LiP() || caseData.getRespondent2SameLegalRepresentative() == YesOrNo.NO))) {
            errors.add("This action is not available for this claim");
        }
    }
}
