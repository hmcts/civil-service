package uk.gov.hmcts.reform.civil.ga.utils;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

public class OrgPolicyUtils {

    private OrgPolicyUtils() {
        //NO-OP
    }

    public static String getRespondent1SolicitorOrgId(GeneralApplicationCaseData caseData) {
        String orgId = getOrgId(caseData.getRespondent1OrganisationPolicy());
        return orgId != null ? orgId : caseData.getRespondent1OrganisationIDCopy();
    }

    public static String getRespondent2SolicitorOrgId(GeneralApplicationCaseData caseData) {
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
