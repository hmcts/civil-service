package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

@Slf4j
public class CaseServiceUtil {

    private CaseServiceUtil() {
        //NO-OP
    }

    public static String getCaseServiceId(CaseCategory  caseCategory) {
        if (UNSPEC_CLAIM.equals(caseCategory)) {
            return "AAA7";
        }
        return "AAA6";
    }

    public static String getCaseServiceId(GeneralApplicationCaseData caseData) {
        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory()) || "UNSPEC_CLAIM".equals(caseData.getGeneralAppSuperClaimType())) {
            return "AAA7";
        }
        return "AAA6";
    }
}
