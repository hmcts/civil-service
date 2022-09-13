package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.model.CaseData;

public class CaseCategoryUtils {

    private CaseCategoryUtils() {
        //NO-OP
    }

    public static boolean isSpecCaseCategory(CaseData caseData, boolean isAccessProfilesEnabled) {
        if (isAccessProfilesEnabled) {
            return CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory());
        } else {
            return SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType());
        }
    }
}
