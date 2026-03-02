package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RecipientData;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

public class NocNotificationUtils {

    private NocNotificationUtils() {
        //NO-OP
    }

    /**
     * Get Change of representation former email address.
     * @param caseData data with change of representation in.
     * @return string email of the former solicitor.
     */
    public static String getPreviousSolicitorEmail(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getFormerRepresentationEmailAddress();
    }

    /**
     * will return applicant 1 details if the changed solicitor isnt applicant one.
     * @param caseData data with the changed solicitor.
     * @return Recipient data model that contains a String email and a String Organisation id, else returns null if
     *     the part is a LiP.
     */
    private static RecipientData getOtherSolicitor1(CaseData caseData) {
        if (isRespondent2NewSolicitor(caseData)) {
            return new RecipientData()
                .setEmail(caseData.getApplicantSolicitor1UserDetailsEmail())
                .setOrgId(caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID());
        } else if (isApplicant1NewSolicitor(caseData)) {
            if (!isOtherPartyLip(caseData.getRespondent1OrganisationPolicy())) {
                Organisation respondent1Org = caseData.getRespondent1OrganisationPolicy().getOrganisation();
                String respondent1OrgID = respondent1Org != null
                    ? respondent1Org.getOrganisationID() : caseData.getRespondent1OrganisationIDCopy();
                return new RecipientData()
                    .setEmail(caseData.getRespondentSolicitor1EmailAddress())
                    .setOrgId(respondent1OrgID);
            }
        } else if (isRespondent1NewSolicitor(caseData) && !caseData.isApplicantLipOneVOne()) {
            return new RecipientData()
                .setEmail(caseData.getApplicantSolicitor1UserDetails().getEmail())
                .setOrgId(caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID());
        }
        return null;
    }

    /**
     * Gets the other solicitor 2 to compliment other solicitor 1 if its a 1v1. if its a 2v1 itll get the applicant 1
     * solicitor as oppose to applicant2 as theyre the same solicitor.
     * @param caseData data that contains which party has changed
     * @return Recipient data model that contains a String email and a String Organisation id, else returns null if
     *     the part is a LiP.
     */
    private static RecipientData getOtherSolicitor2(CaseData caseData) {
        if (isRespondent2NewSolicitor(caseData)) {
            if (!isOtherPartyLip(caseData.getRespondent1OrganisationPolicy())) {
                Organisation respondent1Org = caseData.getRespondent1OrganisationPolicy().getOrganisation();
                String respondent1OrgID = respondent1Org != null
                    ? respondent1Org.getOrganisationID() : caseData.getRespondent1OrganisationIDCopy();
                return new RecipientData()
                    .setEmail(caseData.getRespondentSolicitor1EmailAddress())
                    .setOrgId(respondent1OrgID);
            }
        } else {
            if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
                return new RecipientData()
                    .setEmail(caseData.getApplicantSolicitor1UserDetails().getEmail())
                    .setOrgId(caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID());
            } else {
                if (!isOtherPartyLip(caseData.getRespondent2OrganisationPolicy())) {
                    Organisation respondent2Org = caseData.getRespondent2OrganisationPolicy().getOrganisation();
                    String respondent2OrgID = respondent2Org != null ? respondent2Org.getOrganisationID() : caseData.getRespondent2OrganisationIDCopy();
                    return new RecipientData()
                        .setEmail(caseData.getRespondentSolicitor2EmailAddress())
                        .setOrgId(respondent2OrgID);
                }
            }
        }
        return null;
    }

    public static String getOtherSolicitor1Email(CaseData caseData) {
        RecipientData otherSolicitor1 = getOtherSolicitor1(caseData);
        if (otherSolicitor1 != null) {
            return otherSolicitor1.getEmail();
        }
        if (caseData.isApplicantLipOneVOne() && isRespondent1NewSolicitor(caseData)) {
            return caseData.getApplicant1Email();
        }
        return null;
    }

    public static String getClaimantLipEmail(CaseData caseData) {
        if (caseData.isApplicantLipOneVOne()) {
            return caseData.getApplicant1Email();
        }
        return null;
    }

    public static String getOtherSolicitor1Name(CaseData caseData) {
        RecipientData otherSolicitor1 = getOtherSolicitor1(caseData);
        if (otherSolicitor1 != null) {
            return otherSolicitor1.getOrgId();
        }
        return null;
    }

    public static String getOtherSolicitor2Email(CaseData caseData) {
        RecipientData otherSolicitor2 = getOtherSolicitor2(caseData);
        if (otherSolicitor2 != null) {
            return otherSolicitor2.getEmail();
        }
        return null;
    }

    public static String getOtherSolicitor2Name(CaseData caseData) {
        RecipientData otherSolicitor2 = getOtherSolicitor2(caseData);
        if (otherSolicitor2 != null) {
            return otherSolicitor2.getOrgId();
        }
        return null;
    }

    public static boolean isAppliantLipForRespondentSolicitorChange(CaseData caseData) {
        return caseData.isApplicantLipOneVOne() && isRespondent1NewSolicitor(caseData);
    }

    private static boolean isRespondent2NewSolicitor(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getCaseRole()
            .equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
    }

    private static boolean isApplicant1NewSolicitor(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getCaseRole()
            .equals(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
    }

    private static boolean isRespondent1NewSolicitor(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getCaseRole()
            .equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
    }

    /**
     * checks if the organisation policy is null to determine if the party is LiP or not.
     * @param organisationToCheck Object to check null
     * @return true if the organisation is null meaning the party is a LiP
     */
    private static boolean isOtherPartyLip(OrganisationPolicy organisationToCheck) {
        return organisationToCheck == null
            || organisationToCheck.getOrganisation() == null;
    }

    /**
     * checks the value of the other solicitor 1 to determine if the other party lip check was true or not to skip
     * the notification process.
     * @param caseData data containing the other solicitor 1
     * @return true if the other party 1 is a LiP
     */
    public static boolean isOtherParty1Lip(CaseData caseData) {
        return getOtherSolicitor1(caseData) == null;
    }

    /**
     * checks the value of the other solicitor 2 to determine if the other party lip check was true or not to skip
     * the notification process.
     * @param caseData data containing the other solicitor 2
     * @return true if the other party 2 is a LiP
     */
    public static boolean isOtherParty2Lip(CaseData caseData) {
        return getOtherSolicitor2(caseData) == null;
    }

    /**
     * Builds the case name based on the applicant 1 and applicant 2 if there is one vs respondent 1 and respondent 2
     * if there is one.
     * @param caseData data with the party names in to build the case name
     * @return String of the party names concatenated
     */
    public static String getCaseName(CaseData caseData) {
        String applicants = caseData.getApplicant1().getPartyName();
        if (caseData.getApplicant2() != null) {
            applicants += String.format(", %s", caseData.getApplicant2().getPartyName());
        }
        String defendants = caseData.getRespondent1().getPartyName();
        if (caseData.getRespondent2() != null) {
            defendants += String.format(", %s", caseData.getRespondent2().getPartyName());
        }

        return String.format("%s v %s", applicants, defendants);
    }

    public static CaseData getCaseDataWithoutFormerSolicitorEmail(CaseData caseData) {
        caseData.getChangeOfRepresentation().setFormerRepresentationEmailAddress(null);
        return caseData;
    }
}
