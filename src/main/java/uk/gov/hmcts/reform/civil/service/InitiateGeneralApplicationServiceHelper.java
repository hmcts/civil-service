package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
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

        return caseData.getApplicantSolicitor1UserDetails() != null
                && caseData.getApplicant1OrganisationPolicy() != null
                && isEmailIDSameAsUser(caseData.getApplicantSolicitor1UserDetails().getEmail(), userDetails);
    }

    public boolean isGA_ApplicantSameAsPC_Respondent(CaseData caseData, UserDetails userDetails) {
        return caseData.getRespondentSolicitor1EmailAddress() != null
                && isEmailIDSameAsUser(caseData.getRespondentSolicitor1EmailAddress(), userDetails);
    }

    public GeneralApplication setApplicantAndRespondentDetailsIfExits(GeneralApplication generalApplication,
                                                                      CaseData caseData, UserDetails userDetails) {

        GeneralApplication.GeneralApplicationBuilder applicationBuilder = generalApplication.toBuilder();

        boolean isGAApplicantSameAsParentCaseApplicant = isGA_ApplicantSameAsPC_Applicant(caseData, userDetails);

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
                .respondentSolicitor1EmailAddress(isGAApplicantSameAsParentCaseRespondent
                        ? caseData.getApplicantSolicitor1UserDetails().getEmail()
                        : caseData.getRespondentSolicitor1EmailAddress()).build();
    }

}
