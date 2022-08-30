package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RecipientData;

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
        } else if (caseData.getChangeOfRepresentation().getCaseRole()
            .equals(CaseRole.APPLICANTSOLICITORONE.getFormattedName())) {
            return RecipientData.builder()
                .email(caseData.getRespondentSolicitor2EmailAddress())
                .orgId(caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID())
                .build();
        } else {
            return RecipientData.builder()
                .email(caseData.getRespondentSolicitor1EmailAddress())
                .orgId(caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                .build();
        }
    }

    private static RecipientData getOtherSolicitor2(CaseData caseData) {
        if (isRespondent2NewSolicitor(caseData)) {
            return RecipientData.builder().email(caseData.getRespondentSolicitor1EmailAddress())
                .orgId(caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                .build();
        } else {
            return RecipientData.builder().email(caseData.getRespondentSolicitor2EmailAddress())
                .orgId(caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID())
                .build();
        }
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
