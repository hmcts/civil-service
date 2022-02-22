package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class InitiateGeneralApplicationServiceHelper {

    public boolean isEmailIDSameAsUser(String email, UserDetails userDetails) {

        return StringUtils.isNotBlank(email)
                && userDetails.getEmail().equals(email);
    }

    public boolean isGA_ApplicantSameAsPC_Applicant(CaseData caseData, UserDetails userDetails) {

        return caseData.getApplicantSolicitor1UserDetails() != null
                && caseData.getApplicant1OrganisationPolicy() != null
                && isEmailIDSameAsUser(caseData.getApplicantSolicitor1UserDetails().getEmail(), userDetails);
    }

    public boolean isGA_ApplicantSameAsPC_Respondent(CaseData caseData, UserDetails userDetails) {
        return caseData.getRespondentSolicitor1EmailAddress() != null
                && isEmailIDSameAsUser(caseData.getRespondentSolicitor1EmailAddress(), userDetails);
    }

    public IdamUserDetails constructRespondent1SolicitorUserDetails(UserDetails userDetails) {
        return IdamUserDetails.builder().email(userDetails.getEmail())
                .id(userDetails.getId())
                .build();
    }

    public IdamUserDetails constructApplicant1SolicitorUserDetails(UserDetails userDetails) {
        return IdamUserDetails.builder().email(userDetails.getEmail())
                .id(userDetails.getId())
                .build();
    }

    public boolean validateUserDetails(IdamUserDetails idamUserDetails) {

        return idamUserDetails.getEmail() != null
                && idamUserDetails.getId() != null;
    }

    public GeneralApplication setApplicantAndRespondentDetailsIfExits(GeneralApplication generalApplication,
                                                                      CaseData caseData, UserDetails userDetails) {

        GeneralApplication.GeneralApplicationBuilder applicationBuilder = generalApplication.toBuilder();

        boolean isGAApplicantSameAsParentCaseApplicant = isGA_ApplicantSameAsPC_Applicant(caseData, userDetails);
        applicationBuilder.isPCClaimantMakingApplication(isGAApplicantSameAsParentCaseApplicant ? YES : NO);

        boolean isPCApplicantUserDetailsPresent = validateUserDetails(caseData
                .getApplicantSolicitor1UserDetails());

        boolean isGAApplicantSameAsParentCaseRespondent = isGA_ApplicantSameAsPC_Respondent(caseData, userDetails);

        if (isGAApplicantSameAsParentCaseApplicant
                && isGAApplicantSameAsParentCaseRespondent) {
            return applicationBuilder.build();
        }

        if (!isGAApplicantSameAsParentCaseApplicant
                && !isGAApplicantSameAsParentCaseRespondent) {
            return applicationBuilder.build();
        }

        return applicationBuilder
                .applicantSolicitor1UserDetails(isGAApplicantSameAsParentCaseApplicant
                        ? (isPCApplicantUserDetailsPresent
                        ? caseData.getApplicantSolicitor1UserDetails()
                        : constructApplicant1SolicitorUserDetails(userDetails))
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
