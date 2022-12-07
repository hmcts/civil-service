package uk.gov.hmcts.reform.civil.utils;

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
            return RecipientData.builder()
                .email(caseData.getApplicantSolicitor1UserDetails().getEmail())
                .orgId(caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID())
                .build();
        } else if (isApplicant1NewSolicitor(caseData)) {
            if (!isOtherPartyLip(caseData.getRespondent1OrganisationPolicy())) {
                return RecipientData.builder()
                    .email(caseData.getRespondentSolicitor1EmailAddress())
                    .orgId(caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .build();
            }
        } else if (isRespondent1NewSolicitor(caseData)) {
            return RecipientData.builder()
                .email(caseData.getApplicantSolicitor1UserDetails().getEmail())
                .orgId(caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID())
                .build();
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
                return RecipientData.builder()
                    .email(caseData.getRespondentSolicitor1EmailAddress())
                    .orgId(caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .build();
            }
        } else {
            if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
                return RecipientData.builder()
                    .email(caseData.getApplicantSolicitor1UserDetails().getEmail())
                    .orgId(caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .build();
            } else {
                if (!isOtherPartyLip(caseData.getRespondent2OrganisationPolicy())) {
                    return RecipientData.builder()
                        .email(caseData.getRespondentSolicitor2EmailAddress())
                        .orgId(caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID())
                        .build();
                }
            }
        }
        return null;
    }

    public static String getOtherSolicitor1Email(CaseData caseData) {
        return getOtherSolicitor1(caseData).getEmail();
    }

    public static String getOtherSolicitor1Name(CaseData caseData) {
        return getOtherSolicitor1(caseData).getOrgId();
    }

    public static String getOtherSolicitor2Email(CaseData caseData) {
        return getOtherSolicitor2(caseData).getEmail();
    }

    public static String getOtherSolicitor2Name(CaseData caseData) {
        return getOtherSolicitor2(caseData).getOrgId();
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
        return organisationToCheck == null;
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
}
