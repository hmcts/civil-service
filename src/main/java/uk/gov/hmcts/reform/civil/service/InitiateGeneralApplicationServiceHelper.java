package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Service
@RequiredArgsConstructor
public class InitiateGeneralApplicationServiceHelper {

    public boolean isEmailIDSameAsUser(String email, UserDetails userDetails) {

        return StringUtils.isNotBlank(email)
            && userDetails.getEmail().equals(email);
    }

    public boolean isGA_ApplicantSameAsPC_Applicant(CaseData caseData, UserDetails userDetails) {

        if (caseData.getApplicantSolicitor1UserDetails() != null
            && caseData.getApplicant1OrganisationPolicy() != null
            && isEmailIDSameAsUser(caseData.getApplicantSolicitor1UserDetails().getEmail(), userDetails)) {
            return true;
        }
        return false;
    }

    public boolean isGA_ApplicantSameAsPC_Respondent(CaseData caseData, UserDetails userDetails) {
        if (caseData.getRespondentSolicitor1EmailAddress() != null
            && isEmailIDSameAsUser(caseData.getRespondentSolicitor1EmailAddress(), userDetails)) {
            return true;
        }
        return false;
    }

    public IdamUserDetails constructRespondent1SolicitorUserDetails(UserDetails userDetails) {
        IdamUserDetails applicantDetails = IdamUserDetails.builder().build();
        applicantDetails.toBuilder().email(userDetails.getEmail()).id(userDetails.getId()).build();

        return applicantDetails;
    }

    public GeneralApplication setApplicantAndRespondentDetailsIfExits(GeneralApplication generalApplication,
                                                                      CaseData caseData, UserDetails userDetails) {

        boolean isGAApplicantSameAsParentCaseApplicant = isGA_ApplicantSameAsPC_Applicant(caseData, userDetails);

        boolean isGAApplicantSameAsParentCaseRespondent = isGA_ApplicantSameAsPC_Respondent(caseData, userDetails);

        if (isGAApplicantSameAsParentCaseApplicant
            && isGAApplicantSameAsParentCaseRespondent) {
            return generalApplication;
        }

        if (!isGAApplicantSameAsParentCaseApplicant
            && !isGAApplicantSameAsParentCaseRespondent) {
            return generalApplication;
        }

        return generalApplication.toBuilder()
            .applicantSolicitor1UserDetails(isGAApplicantSameAsParentCaseApplicant
                                                ? caseData.getApplicantSolicitor1UserDetails()
                                                : constructRespondent1SolicitorUserDetails(userDetails))
            .applicant1OrganisationPolicy(isGAApplicantSameAsParentCaseApplicant
                                              ? caseData.getApplicant1OrganisationPolicy()
                                              : caseData.getRespondent1OrganisationPolicy())
            .respondent1OrganisationPolicy(isGAApplicantSameAsParentCaseRespondent
                                               ? caseData.getApplicant1OrganisationPolicy()
                                               : caseData.getRespondent1OrganisationPolicy())
            .respondentSolicitor1EmailAddress(isGAApplicantSameAsParentCaseRespondent
                                                  ? caseData.getApplicantSolicitor1UserDetails().getEmail()
                                                  : caseData.getRespondentSolicitor1EmailAddress()).build();

    }

}
