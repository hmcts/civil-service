package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

public class HmctsServiceIDUtils {

    private HmctsServiceIDUtils() {
        //NO-OP
    }

    public static String getHmctsServiceID(CaseData caseData, PaymentsConfiguration paymentsConfiguration) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return paymentsConfiguration.getSpecSiteId();
        } else if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return paymentsConfiguration.getSiteId();
        }
        return null;
    }
}
