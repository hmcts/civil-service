package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;

public class NocNotificationUtils {

    private static String otherSolicitor1Email;
    private static String otherSolicitor1OrgId;
    private static String otherSolicitor2Email;
    private static String otherSolicitor2OrgId;

    private NocNotificationUtils() {
        //NO-OP
    }

    public static String getPreviousSolicitorEmail(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getFormerRepresentationEmailAddress();
    }

    private static void setOtherSolicitor1(CaseData caseData) {
        if (isRespondent2NewSolicitor(caseData)) {
            otherSolicitor1Email = caseData.getApplicantSolicitor1UserDetails().getEmail();
            otherSolicitor1OrgId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        } else if (caseData.getChangeOfRepresentation().getCaseRole().equals(CaseRole.APPLICANTSOLICITORONE)) {
            otherSolicitor1Email = caseData.getRespondentSolicitor2EmailAddress();
            otherSolicitor1OrgId = caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        } else {
            otherSolicitor1Email = caseData.getRespondentSolicitor1EmailAddress();
            otherSolicitor1OrgId = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        }
    }

    private static void setOtherSolicitor2(CaseData caseData) {
        if (isRespondent2NewSolicitor(caseData)) {
            otherSolicitor2Email = caseData.getRespondentSolicitor1EmailAddress();
            otherSolicitor2OrgId = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        } else {
            otherSolicitor2Email = caseData.getRespondentSolicitor2EmailAddress();
            otherSolicitor2OrgId = caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        }
    }

    public static String getOtherSolicitor1Email(CaseData caseData) {
        setOtherSolicitor1(caseData);
        return otherSolicitor1Email;
    }

    public static String getOtherSolicitor1Name(CaseData caseData) {
        setOtherSolicitor1(caseData);
        return otherSolicitor1OrgId;
    }

    public static String getOtherSolicitor2Email(CaseData caseData) {
        setOtherSolicitor2(caseData);
        return otherSolicitor2Email;
    }

    public static String getOtherSolicitor2Name(CaseData caseData) {
        setOtherSolicitor2(caseData);
        return otherSolicitor2OrgId;
    }

    public static boolean isRespondent2NewSolicitor(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getCaseRole().equals(CaseRole.RESPONDENTSOLICITORTWO);
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
