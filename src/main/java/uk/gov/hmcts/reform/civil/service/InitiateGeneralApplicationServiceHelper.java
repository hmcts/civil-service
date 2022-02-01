package uk.gov.hmcts.reform.civil.service;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

public class InitiateGeneralApplicationServiceHelper {

    public boolean isApplicantSolicitorEmailExits(String email, UserDetails userDetails) {

        return StringUtils.isNotBlank(email)
            && userDetails.getEmail().equals(email);
    }

    public boolean isGA_ApplicantSameAsPC_Applicant(CaseData caseData, UserDetails userDetails) {

        if (caseData.getApplicantSolicitor1UserDetails() != null
            && caseData.getApplicant1OrganisationPolicy() != null
            && isApplicantSolicitorEmailExits(caseData.getApplicantSolicitor1UserDetails().getEmail(), userDetails)) {
            return true;
        }
        return false;
    }

    public boolean isGA_ApplicantSameAsPC_Respondent(CaseData caseData, UserDetails userDetails) {
        if (caseData.getRespondentSolicitor1EmailAddress() != null
            && isApplicantSolicitorEmailExits(caseData.getRespondentSolicitor1EmailAddress(), userDetails)) {
            return true;
        }
        return false;
    }

    public boolean isGA_ApplicantOrgSameAsPC_RespondentOrg(CaseData caseData, UserDetails userDetails) {
        if (caseData.getRespondent1OrganisationPolicy() != null
            && isApplicantSolicitorEmailExits(caseData.getRespondentSolicitor1EmailAddress(), userDetails)) {
            return true;
        }
        return false;
    }

    public IdamUserDetails constructRespondent1SolicitorUserDetails(UserDetails userDetails) {
        IdamUserDetails applicantDetails = IdamUserDetails.builder().build();
        applicantDetails.toBuilder().email(userDetails.getEmail()).id(userDetails.getId()).build();

        return applicantDetails;
    }
}
