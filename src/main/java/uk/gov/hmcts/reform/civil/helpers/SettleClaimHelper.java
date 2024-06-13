package uk.gov.hmcts.reform.civil.helpers;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.List;

public class SettleClaimHelper {

    private SettleClaimHelper() {
        // Utility class, no instances
    }

    public static List<String> checkCaseType(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.isApplicantLiP()
            || MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)
            || (caseData.getAddRespondent2() == YesOrNo.YES && (caseData.isRespondent1LiP() || caseData.isRespondent2LiP()))) {
            errors.add("This action is not available for this claim");
        }
        return errors;
    }
}
