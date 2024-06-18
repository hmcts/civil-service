package uk.gov.hmcts.reform.civil.helpers;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

public class DiscontinueClaimHelper {

    private DiscontinueClaimHelper() {
        // Utility class, no instances
    }

    public static void checkState(CaseData caseData, List<String> errors) {

        if (caseData.isApplicantLiP()
            || MultiPartyScenario.isTwoVOne(caseData)) {
            errors.add("This action is not available for this claim");
        }
    }
}
