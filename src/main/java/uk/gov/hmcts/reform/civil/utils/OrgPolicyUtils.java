package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisation;
import uk.gov.hmcts.reform.civil.model.CaseData;
import java.util.Comparator;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

public class OrgPolicyUtils {

    private OrgPolicyUtils() {
        //NO-OP
    }

    public static String getRespondent1SolicitorOrgId(CaseData caseData) {
        return getOrgId(caseData.getRespondent1OrganisationPolicy(), caseData.getRespondent1OrganisationIDCopy());
    }

    public static String getRespondent2SolicitorOrgId(CaseData caseData) {
        return getOrgId(caseData.getRespondent2OrganisationPolicy(), caseData.getRespondent2OrganisationIDCopy());
    }

    private static String getOrgId(OrganisationPolicy orgPolicy, String orgIdCopy) {
        if (orgPolicy != null && orgPolicy.getOrganisation() != null && orgPolicy.getOrganisation().getOrganisationID() != null) {
            return orgPolicy.getOrganisation().getOrganisationID();
        } else {
            return orgIdCopy;
        }
    }

    public static void addMissingOrgPolicies(CaseData.CaseDataBuilder dataBuilder) {
        CaseData caseData = dataBuilder.build();
        if (caseData.getRespondent1OrganisationPolicy() == null) {
            dataBuilder
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE.getFormattedName())
                                                   .build());
        }
        if (caseData.getRespondent2OrganisationPolicy() == null) {
            dataBuilder
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                   .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO.getFormattedName())
                                                   .build());
        }
    }

    public static PreviousOrganisation getLatestOrganisationChanges(OrganisationPolicy organisationPolicy) {
        if (organisationPolicy != null && organisationPolicy.getPreviousOrganisations() != null) {
            return organisationPolicy.getPreviousOrganisations().stream()
                .map(orgCollectionObject -> orgCollectionObject.getValue())
                .max(Comparator.comparing(PreviousOrganisation::getToTimestamp)).orElse(null);
        } else {
            return null;
        }
    }
}
