package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.HmctsServiceId.SPEC_SERVICE_ID;
import static uk.gov.hmcts.reform.civil.enums.HmctsServiceId.UNSPEC_SERVICE_ID;

public class HMCTSServiceIDUtils {

    private HMCTSServiceIDUtils() {
        //NO-OP
    }

    public static String getHmctsServiceID(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return SPEC_SERVICE_ID.getServiceId();
        } else if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return UNSPEC_SERVICE_ID.getServiceId();
        }
        return null;
    }
}
