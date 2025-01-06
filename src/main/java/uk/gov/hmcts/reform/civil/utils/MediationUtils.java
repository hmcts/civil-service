package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;
import uk.gov.hmcts.reform.civil.model.CaseData;
import java.util.List;

public class MediationUtils {

    private MediationUtils() {
        //NO OP
    }

    public static boolean findMediationUnsuccessfulReason(CaseData caseData,
                                                    List<MediationUnsuccessfulReason> listOfReasons) {
        if(caseData.getMediation() == null || caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect() == null) {
            return false;
        }
        return caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect()
            .stream().anyMatch(listOfReasons::contains);
    }
}
