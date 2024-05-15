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
        return caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect()
            .stream().anyMatch(listOfReasons::contains);
    }
}
