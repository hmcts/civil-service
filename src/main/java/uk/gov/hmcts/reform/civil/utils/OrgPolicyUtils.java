package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;

public class OrgPolicyUtils {

    private OrgPolicyUtils() {
        //NO-OP
    }

    public static String getRespondent1SolicitorOrgId(CaseData caseData) {
        return caseData.getRespondent1OrganisationIDCopy() != null
            ? caseData.getRespondent1OrganisationIDCopy()
            : caseData.getRespondent1OrganisationPolicy() != null
            ? caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
            : null;
    }

    public static String getRespondent2SolicitorOrgId(CaseData caseData) {
        return caseData.getRespondent2OrganisationIDCopy() != null
            ? caseData.getRespondent2OrganisationIDCopy()
            : caseData.getRespondent2OrganisationPolicy() != null
            ? caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID()
            : null;
    }
}
