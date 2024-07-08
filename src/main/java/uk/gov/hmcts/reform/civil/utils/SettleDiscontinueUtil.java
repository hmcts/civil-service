package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.model.CaseData;

public class SettleDiscontinueUtil {

    private SettleDiscontinueUtil() {
        //NO-OP
    }
    public static boolean hasCourtPermissionGranted(CaseData caseData) {
        return caseData.getIsPermissionGranted().equals(SettleDiscontinueYesOrNoList.YES);
    }
}
