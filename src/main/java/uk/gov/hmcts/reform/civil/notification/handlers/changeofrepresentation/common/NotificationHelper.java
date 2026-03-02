package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.common;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RecipientData;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

public class NotificationHelper {

    private NotificationHelper() {
        //NO-OP
    }

    public static final String LIP = "LiP";

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

    public static String getOtherSolicitor2Name(CaseData caseData) {
        RecipientData otherSolicitor2 = getOtherSolicitor2(caseData);
        if (otherSolicitor2 != null) {
            return otherSolicitor2.getOrgId();
        }
        return null;
    }

    /**
     * Gets the other solicitor 2 to compliment other solicitor 1 if its a 1v1. if its a 2v1 it'll get the applicant 1
     * solicitor as oppose to applicant2 as theyre the same solicitor.
     * @param caseData data that contains which party has changed
     * @return Recipient data model that contains a String email and a String Organisation id, else returns null if
     *     the part is a LiP.
     */
    protected static RecipientData getOtherSolicitor2(CaseData caseData) {
        if (isRespondent2NewSolicitor(caseData)) {
            if (!isOtherPartyLip(caseData.getRespondent1OrganisationPolicy())) {
                String respondent1OrgID = Optional.ofNullable(caseData.getRespondent1OrganisationPolicy())
                    .map(OrganisationPolicy::getOrganisation)
                    .map(Organisation::getOrganisationID)
                    .orElse(caseData.getRespondent1OrganisationIDCopy());
                return new RecipientData()
                    .setEmail(caseData.getRespondentSolicitor1EmailAddress())
                    .setOrgId(respondent1OrgID);
            }
        } else {
            if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
                String applicantOrgId = Optional.ofNullable(caseData.getApplicant1OrganisationPolicy())
                    .map(OrganisationPolicy::getOrganisation)
                    .map(Organisation::getOrganisationID)
                    .orElse(null);
                return new RecipientData()
                    .setEmail(caseData.getApplicantSolicitor1UserDetailsEmail())
                    .setOrgId(applicantOrgId);
            } else {
                if (!isOtherPartyLip(caseData.getRespondent2OrganisationPolicy())) {
                    String respondent2OrgID = Optional.ofNullable(caseData.getRespondent2OrganisationPolicy())
                        .map(OrganisationPolicy::getOrganisation)
                        .map(Organisation::getOrganisationID)
                        .orElse(caseData.getRespondent2OrganisationIDCopy());
                    return new RecipientData()
                        .setEmail(caseData.getRespondentSolicitor2EmailAddress())
                        .setOrgId(respondent2OrgID);
                }
            }
        }
        return null;
    }

    private static boolean isRespondent2NewSolicitor(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getCaseRole()
            .equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
    }

    /**
     * checks if the organisation policy is null to determine if the party is LiP or not.
     * @param organisationToCheck Object to check null
     * @return true if the organisation is null meaning the party is a LiP
     */
    protected static boolean isOtherPartyLip(OrganisationPolicy organisationToCheck) {
        return organisationToCheck == null
            || organisationToCheck.getOrganisation() == null;
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
     * checks the value of the other solicitor 1 to determine if the other party lip check was true or not to skip
     * the notification process.
     * @param caseData data containing the other solicitor 1
     * @return true if the other party 1 is a LiP
     */
    public static boolean isOtherParty1Lip(CaseData caseData) {
        return getOtherSolicitor1(caseData) == null;
    }

    public static String getOtherSolicitor1Name(CaseData caseData) {
        RecipientData otherSolicitor1 = getOtherSolicitor1(caseData);
        if (otherSolicitor1 != null) {
            return otherSolicitor1.getOrgId();
        }
        return null;
    }

    /**
     * will return applicant 1 details if the changed solicitor isnt applicant one.
     * @param caseData data with the changed solicitor.
     * @return Recipient data model that contains a String email and a String Organisation id, else returns null if
     *     the part is a LiP.
     */
    protected static RecipientData getOtherSolicitor1(CaseData caseData) {
        if (isRespondent2NewSolicitor(caseData)) {
            String applicantOrgId = Optional.ofNullable(caseData.getApplicant1OrganisationPolicy())
                .map(OrganisationPolicy::getOrganisation)
                .map(Organisation::getOrganisationID)
                .orElse(null);
            return new RecipientData()
                .setEmail(caseData.getApplicantSolicitor1UserDetailsEmail())
                .setOrgId(applicantOrgId);
        } else if (isApplicant1NewSolicitor(caseData)) {
            if (!isOtherPartyLip(caseData.getRespondent1OrganisationPolicy())) {
                String respondent1OrgID = Optional.ofNullable(caseData.getRespondent1OrganisationPolicy())
                    .map(OrganisationPolicy::getOrganisation)
                    .map(Organisation::getOrganisationID)
                    .orElseGet(caseData::getRespondent1OrganisationIDCopy);
                return new RecipientData()
                    .setEmail(caseData.getRespondentSolicitor1EmailAddress())
                    .setOrgId(respondent1OrgID);
            }
        } else if (isRespondent1NewSolicitor(caseData) && !caseData.isApplicantLipOneVOne()) {
            String applicantOrgId = Optional.ofNullable(caseData.getApplicant1OrganisationPolicy())
                .map(OrganisationPolicy::getOrganisation)
                .map(Organisation::getOrganisationID)
                .orElse(null);
            return new RecipientData()
                .setEmail(caseData.getApplicantSolicitor1UserDetailsEmail())
                .setOrgId(applicantOrgId);
        }
        return null;
    }

    protected static boolean isApplicant1NewSolicitor(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getCaseRole()
            .equals(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
    }

    protected static boolean isRespondent1NewSolicitor(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getCaseRole()
            .equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
    }

    public static boolean isApplicantLipForRespondentSolicitorChange(CaseData caseData) {
        return caseData.isApplicantLipOneVOne() && isRespondent1NewSolicitor(caseData);
    }

    /**
     * Get Change of representation former email address.
     * @param caseData data with change of representation in.
     * @return string email of the former solicitor.
     */
    public static String getPreviousSolicitorEmail(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getFormerRepresentationEmailAddress();
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

    public static String getOtherSolicitor2Email(CaseData caseData) {
        RecipientData otherSolicitor2 = getOtherSolicitor2(caseData);
        if (otherSolicitor2 != null) {
            return otherSolicitor2.getEmail();
        }
        return null;
    }
}
