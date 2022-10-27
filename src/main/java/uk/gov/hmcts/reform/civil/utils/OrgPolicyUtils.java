package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;

public class OrgPolicyUtils {

    private OrgPolicyUtils() {
        //NO-OP
    }

    public static String getRespondent1SolicitorOrgId(CaseData caseData) {
        String orgId = getOrgId(caseData.getRespondent1OrganisationPolicy());
        return orgId != null ? orgId : caseData.getRespondent1OrganisationIDCopy();
    }

    public static String getRespondent2SolicitorOrgId(CaseData caseData) {
        String orgId = getOrgId(caseData.getRespondent2OrganisationPolicy());
        return orgId != null ? orgId : caseData.getRespondent2OrganisationIDCopy();
    }

    private static String getOrgId(OrganisationPolicy orgPolicy) {
        return orgPolicy != null
            && orgPolicy.getOrganisation() != null
            && orgPolicy.getOrganisation().getOrganisationID() != null
            ? orgPolicy.getOrganisation().getOrganisationID() : null;
    }
}
