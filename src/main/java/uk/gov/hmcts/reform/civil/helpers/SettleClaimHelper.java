package uk.gov.hmcts.reform.civil.helpers;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
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
            || (MultiPartyScenario.isOneVTwoLegalRep(caseData) && caseData.isRespondent1LiP())) {
            errors.add("This action is not available for this claim");
        }
    }
}
