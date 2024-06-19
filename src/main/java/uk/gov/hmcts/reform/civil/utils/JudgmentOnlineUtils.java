package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class JudgmentOnlineUtils {

    private JudgmentOnlineUtils() {
        //NO-OP
    }

    public static Optional<Organisation> getOrganisationByPolicy(OrganisationPolicy organisationPolicy, OrganisationService organisationService) {
        String orgId = organisationPolicy.getOrganisation().getOrganisationID();
        return organisationService.findOrganisationById(orgId);
    }

    public static boolean applicant2Present(CaseData caseData) {
        return caseData.getAddApplicant2() != null && caseData.getAddApplicant2() == YES;
    }

    public static boolean respondent2Present(CaseData caseData) {
        return caseData.getAddRespondent2() != null
            && caseData.getAddRespondent2() == YES;
    }

    public static boolean areRespondentLegalOrgsEqual(CaseData caseData) {
        return (caseData.getRespondent2() != null
            && caseData.getRespondent1OrganisationPolicy().getOrganisation() != null
            && caseData.getRespondent2OrganisationPolicy().getOrganisation() != null
            && caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID().equals(
            caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID()));
    }

    public static String getApplicantSolicitorRef(CaseData caseData) {
        if (caseData.getSolicitorReferences() != null && caseData.getSolicitorReferences()
            .getApplicantSolicitor1Reference() != null) {
            return caseData.getSolicitorReferences().getApplicantSolicitor1Reference();
        }
        return null;
    }

    public static String getRespondent1SolicitorRef(CaseData caseData) {
        if (caseData.getSolicitorReferences() != null && caseData.getSolicitorReferences()
            .getRespondentSolicitor1Reference() != null) {
            return caseData.getSolicitorReferences().getRespondentSolicitor1Reference();
        }
        return null;
    }

    public static String getRespondent2SolicitorRef(CaseData caseData) {
        if (caseData.getSolicitorReferences() != null && caseData.getSolicitorReferences()
            .getRespondentSolicitor2Reference() != null) {
            return caseData.getSolicitorReferences().getRespondentSolicitor2Reference();
        }
        return null;
    }

    public static List<Party> getApplicant(uk.gov.hmcts.reform.civil.model.Party applicant1,
                                     uk.gov.hmcts.reform.civil.model.Party applicant2) {

        List<Party> applicants = new ArrayList<>();
        applicants.add(Party.builder()
                           .name(applicant1.getPartyName())
                           .primaryAddress(applicant1.getPrimaryAddress())
                           .build());
        if (applicant2 != null) {
            applicants.add(Party.builder()
                               .name(applicant2.getPartyName())
                               .primaryAddress(applicant2.getPrimaryAddress())
                               .build());
        }
        return applicants;
    }
}
