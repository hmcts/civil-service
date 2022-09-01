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

    public static String getPreviousSolicitorEmail(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getFormerRepresentationEmailAddress();
    }

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

    private static boolean isOtherPartyLip(OrganisationPolicy organisationToCheck) {
        return organisationToCheck == null;
    }

    public static boolean isOtherParty1Lip(CaseData caseData) {
        return getOtherSolicitor1(caseData) == null;
    }

    public static boolean isOtherParty2Lip(CaseData caseData) {
        return getOtherSolicitor2(caseData) == null;
    }

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
