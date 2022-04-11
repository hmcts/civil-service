package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class InitiateGeneralApplicationServiceHelper {

    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;

    public boolean isPCClaimantEmailIDSameAsLoginUser(String email, UserDetails userDetails) {

        return StringUtils.isNotBlank(email)
            && userDetails.getEmail().equals(email);
    }

    public boolean isGA_ApplicantSameAsPC_Claimant(CaseData caseData, UserDetails userDetails) {

        return caseData.getApplicantSolicitor1UserDetails() != null
            && caseData.getApplicant1OrganisationPolicy() != null
            && isPCClaimantEmailIDSameAsLoginUser(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            userDetails
        );
    }

    public GeneralApplication setApplicantAndRespondentDetailsIfExits(GeneralApplication generalApplication,
                                                                      CaseData caseData, UserDetails userDetails) {

        GeneralApplication.GeneralApplicationBuilder applicationBuilder = generalApplication.toBuilder();

        boolean isGAApplicantSameAsParentCaseClaimant = isGA_ApplicantSameAsPC_Claimant(caseData, userDetails);

        String parentCaseId = caseData.getCcdCaseReference().toString();

        CaseAssignedUserRolesResource userRoles = caseAccessDataStoreApi.getUserRoles(
            getCaaAccessToken(),
            authTokenGenerator.generate(),
            List.of(parentCaseId)
        );

        List<CaseAssignedUserRole> respondentSolicitors = userRoles.getCaseAssignedUserRoles().stream()
            .filter(CA -> !CA.getUserId().equals(userDetails.getId()))
            .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(respondentSolicitors)) {
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            if (caseData.getApplicant1OrganisationPolicy() == null
                || caseData.getRespondent1OrganisationPolicy() == null
                || (YES.equals(caseData.getAddRespondent2()) && caseData.getRespondent2OrganisationPolicy() == null)) {
                throw new IllegalArgumentException("Solicitor Org details are not set correctly.");
            }
            String applicantOrgCaseRole = caseData.getApplicant1OrganisationPolicy()
                .getOrgPolicyCaseAssignedRole();
            String respondentOrgCaseRole = caseData.getRespondent1OrganisationPolicy()
                .getOrgPolicyCaseAssignedRole();

            respondentSolicitors.forEach((respSol) -> {
                GASolicitorDetailsGAspec.GASolicitorDetailsGAspecBuilder specBuilder = GASolicitorDetailsGAspec
                    .builder();

                specBuilder.id(respSol.getUserId());
                if (respSol.getCaseRole() != null) {
                    if (respSol.getCaseRole().equals(applicantOrgCaseRole)) {
                        if (caseData.getApplicantSolicitor1UserDetails() != null) {
                            specBuilder.email(caseData.getApplicantSolicitor1UserDetails().getEmail());
                        }

                        specBuilder.organisationIdentifier(caseData.getApplicant1OrganisationPolicy()
                                                               .getOrganisation().getOrganisationID());
                    } else if (respSol.getCaseRole().equals(respondentOrgCaseRole)) {
                        specBuilder.email(caseData.getRespondentSolicitor1EmailAddress());
                        specBuilder.organisationIdentifier(caseData.getRespondent1OrganisationPolicy()
                                                               .getOrganisation().getOrganisationID());
                    } else {
                        specBuilder.email(caseData.getRespondentSolicitor2EmailAddress());
                        specBuilder.organisationIdentifier(caseData.getRespondent2OrganisationPolicy()
                                                               .getOrganisation().getOrganisationID());
                    }
                } else {
                    specBuilder.email(StringUtils.EMPTY);
                    specBuilder.organisationIdentifier(StringUtils.EMPTY);
                }

                GASolicitorDetailsGAspec gaSolicitorDetailsGAspec = specBuilder.build();
                respondentSols.add(element(gaSolicitorDetailsGAspec));
            });
            applicationBuilder.generalAppRespondentSolicitors(respondentSols);
        }

        return applicationBuilder
            .parentClaimantIsApplicant(isGAApplicantSameAsParentCaseClaimant
                                           ? YES
                                           : YesOrNo.NO).build();
    }

    public String getCaaAccessToken() {
        return userService.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );
    }

}
