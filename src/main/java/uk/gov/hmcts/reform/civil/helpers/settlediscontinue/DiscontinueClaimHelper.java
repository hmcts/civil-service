package uk.gov.hmcts.reform.civil.helpers.settlediscontinue;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

public class DiscontinueClaimHelper {

    private DiscontinueClaimHelper() {
        // Utility class, no instances
    }

    public static void checkState(CaseData caseData, List<String> errors) {

        if (caseData.isApplicantLiP()
            || (caseData.getAddRespondent2() == YesOrNo.YES && (caseData.isRespondent1LiP() || caseData.isRespondent2LiP()))) {
            errors.add("This action is not available for this claim");
        }
    }

    public static boolean is1v2LrVLrCase(CaseData caseData) {

        return !caseData.isApplicantNotRepresented()
            && !caseData.isRespondent1NotRepresented() && !caseData.isRespondent2NotRepresented()
            && (MultiPartyScenario.isOneVTwoLegalRep(caseData) || MultiPartyScenario.isOneVTwoTwoLegalRep(caseData));
    }
}
