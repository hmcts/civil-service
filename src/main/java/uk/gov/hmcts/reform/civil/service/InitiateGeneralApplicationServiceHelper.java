package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class InitiateGeneralApplicationServiceHelper {

    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;


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

        String parentCaseId = caseData.getCcdCaseReference().toString();

        boolean isGAApplicantSameAsParentCaseRespondent1 = isGA_ApplicantSameAsPC_Respondent(caseData, userDetails);

        if (!isGAApplicantSameAsParentCaseApplicant
            && !isGAApplicantSameAsParentCaseRespondent1) {
            return applicationBuilder.build();
        }

        CaseAssignedUserRolesResource userRoles = caseAccessDataStoreApi.getUserRoles(
            getCaaAccessToken(),
            authTokenGenerator.generate(),
            List.of(parentCaseId)
        );

        List<CaseAssignedUserRole> respondentSolicitors = userRoles.getCaseAssignedUserRoles().stream()
            .filter(CA -> !CA.getUserId().equals(userDetails.getId()))
            .collect(Collectors.toList());

        if (respondentSolicitors != null) {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            respondentSolicitors.stream().forEach((RS) -> {

                GASolicitorDetailsGAspec gaSolicitorDetailsGAspec = GASolicitorDetailsGAspec
                    .builder()
                    .id(RS.getUserId())
                    .email(RS.getCaseRole() == caseData.getApplicant1OrganisationPolicy().getOrgPolicyCaseAssignedRole()
                               ? caseData.getApplicantSolicitor1UserDetails().getEmail()
                               : RS.getCaseRole() == caseData.getRespondent1OrganisationPolicy()
                        .getOrgPolicyCaseAssignedRole()
                        ? caseData.getRespondentSolicitor1EmailAddress()
                        : caseData.getRespondentSolicitor2EmailAddress())
                    .organisationIdentifier(RS.getCaseRole() == caseData.getApplicant1OrganisationPolicy()
                        .getOrgPolicyCaseAssignedRole()
                                                ? caseData.getApplicant1OrganisationPolicy().getOrganisation()
                        .getOrganisationID() : RS.getCaseRole() == caseData.getRespondent1OrganisationPolicy()
                        .getOrgPolicyCaseAssignedRole()
                        ? caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
                        : caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID()).build();

                respondentSols.add(element(gaSolicitorDetailsGAspec));
            });
            applicationBuilder.generalAppRespondentSolictor(respondentSols);
        }

        return applicationBuilder
            .respondentSolicitor1EmailAddress(isGAApplicantSameAsParentCaseRespondent1
                                                  ? caseData.getApplicantSolicitor1UserDetails().getEmail()
                                                  : caseData.getRespondentSolicitor1EmailAddress()).build();
    }

    private String getCaaAccessToken() {
        return userService.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );
    }

}
