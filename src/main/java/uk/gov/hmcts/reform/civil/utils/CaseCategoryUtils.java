package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;

public class CaseCategoryUtils {

    private CaseCategoryUtils() {
        //NO-OP
    }

    /**
     * When accessProfiles is released and accessProfilesEnabled is always true, this method won't be needed.
     *
     * @param caseData                case data
     * @param isAccessProfilesEnabled value of flag isAccessProfilesEnabled
     * @return true if claim is spec, false otherwise
     */
    public static boolean isSpecCaseCategory(CaseData caseData, boolean isAccessProfilesEnabled) {
        return CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory());
    }
}
