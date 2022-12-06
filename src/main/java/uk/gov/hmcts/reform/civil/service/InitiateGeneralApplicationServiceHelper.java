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
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.log;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils.getRespondent1SolicitorOrgId;
import static uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils.getRespondent2SolicitorOrgId;

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

    public GeneralApplication setRespondentDetailsIfPresent(GeneralApplication generalApplication,
                                                            CaseData caseData, UserDetails userDetails) {
        if (caseData.getApplicant1OrganisationPolicy() == null
                || caseData.getRespondent1OrganisationPolicy() == null
                || (YES.equals(caseData.getAddRespondent2()) && caseData.getRespondent2OrganisationPolicy() == null)) {
            throw new IllegalArgumentException("Solicitor Org details are not set correctly.");
        }
        GeneralApplication.GeneralApplicationBuilder applicationBuilder = generalApplication.toBuilder();

        String parentCaseId = caseData.getCcdCaseReference().toString();

        String applicant1OrgCaseRole = caseData.getApplicant1OrganisationPolicy().getOrgPolicyCaseAssignedRole();
        String respondent1OrgCaseRole = caseData.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole();

        CaseAssignedUserRolesResource userRoles = caseAccessDataStoreApi.getUserRoles(
            getCaaAccessToken(), authTokenGenerator.generate(), List.of(parentCaseId));

        /*Filter the case users to collect solicitors whose ID doesn't match with GA Applicant Solicitor's ID*/
        List<CaseAssignedUserRole> respondentSolicitors = userRoles.getCaseAssignedUserRoles().stream()
            .filter(CA -> !CA.getUserId().equals(userDetails.getId()))
            .collect(Collectors.toList());

        /*
         * Set GA applicant solicitor details
         * */
        GASolicitorDetailsGAspec.GASolicitorDetailsGAspecBuilder applicantBuilder = GASolicitorDetailsGAspec
            .builder();

        applicantBuilder
            .id(userDetails.getId())
            .email(userDetails.getEmail())
            .forename(userDetails.getForename())
            .surname(userDetails.getSurname());

        List<CaseAssignedUserRole> applicantSolicitor = userRoles.getCaseAssignedUserRoles()
            .stream().filter(user -> !respondentSolicitors.contains(user)).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(applicantSolicitor) && applicantSolicitor.size() == 1) {

            CaseAssignedUserRole applnSol = applicantSolicitor.get(0);

            if (applnSol.getCaseRole() != null) {

                if (applnSol.getCaseRole().equals(applicant1OrgCaseRole)) {

                    applicantBuilder.organisationIdentifier(caseData.getApplicant1OrganisationPolicy()
                                                                .getOrganisation().getOrganisationID());

                } else if (applnSol.getCaseRole().equals(respondent1OrgCaseRole)) {

                    applicantBuilder.organisationIdentifier(getRespondent1SolicitorOrgId(caseData));

                } else if (caseData.getAddRespondent2().equals(YES)
                    && applnSol.getCaseRole().equals(caseData.getRespondent2OrganisationPolicy()
                                                         .getOrgPolicyCaseAssignedRole())) {

                    applicantBuilder.organisationIdentifier(getRespondent2SolicitorOrgId(caseData));

                } else {
                    if (caseData.getAddApplicant2().equals(YES)
                        && applnSol.getCaseRole().equals(caseData
                                                             .getApplicant2OrganisationPolicy()
                                                             .getOrgPolicyCaseAssignedRole())) {

                        applicantBuilder.organisationIdentifier(caseData.getApplicant2OrganisationPolicy()
                                                                    .getOrgPolicyCaseAssignedRole());

                    }
                }
            }
        }

        applicationBuilder
            .generalAppApplnSolicitor(applicantBuilder.build());

        /*
         * Set GA respondent solicitors' details
         * */
        if (!CollectionUtils.isEmpty(respondentSolicitors)) {
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            respondentSolicitors.forEach((respSol) -> {
                GASolicitorDetailsGAspec.GASolicitorDetailsGAspecBuilder specBuilder = GASolicitorDetailsGAspec
                    .builder();

                specBuilder.id(respSol.getUserId());

                if (respSol.getCaseRole() != null) {
                    /*Populate the GA respondent solicitor details in accordance with civil case Applicant Solicitor 1
                details if case role of collected user matches with case role of Applicant 1*/
                    if (respSol.getCaseRole().equals(applicant1OrgCaseRole)) {
                        if (caseData.getApplicantSolicitor1UserDetails() != null) {
                            specBuilder.email(caseData.getApplicantSolicitor1UserDetails().getEmail());
                            specBuilder.organisationIdentifier(caseData.getApplicant1OrganisationPolicy()
                                                                   .getOrganisation().getOrganisationID());
                        }
                        /*Populate the GA respondent solicitor details in accordance with civil case Respondent
                        Solicitor 1 details if caserole of collected user matches with caserole Respondent Solicitor 1*/
                    } else if (respSol.getCaseRole().equals(respondent1OrgCaseRole)) {
                        specBuilder.email(caseData.getRespondentSolicitor1EmailAddress());
                        specBuilder.organisationIdentifier(getRespondent1SolicitorOrgId(caseData));
                        /*Populate the GA respondent solicitor details in accordance with civil case Respondent
                        Solicitor 2 details if it's 1 V 2 Different Solicitor scenario*/
                    } else {
                        specBuilder.email(caseData.getRespondentSolicitor2EmailAddress());
                        specBuilder.organisationIdentifier(getRespondent2SolicitorOrgId(caseData));
                    }
                    /*Set the GA Respondent solicitor details to Empty if above checks are failed*/
                } else {
                    String errorMsg = String.format(
                        "Invalid User (userId [%s]): Without Case Role ",
                        respSol.getUserId()
                    );
                    log.error(errorMsg);
                    throw new IllegalArgumentException(errorMsg);
                }

                GASolicitorDetailsGAspec gaSolicitorDetailsGAspec = specBuilder.build();
                respondentSols.add(element(gaSolicitorDetailsGAspec));
            });
            applicationBuilder.applicantPartyName(getApplicantPartyName(userRoles, userDetails, caseData));
            applicationBuilder.generalAppRespondentSolicitors(respondentSols);
        }

        boolean isGAApplicantSameAsParentCaseClaimant = isGA_ApplicantSameAsPC_Claimant(caseData, userDetails);

        return applicationBuilder
            .parentClaimantIsApplicant(isGAApplicantSameAsParentCaseClaimant
                                           ? YES
                                           : YesOrNo.NO).build();
    }

    private String getApplicantPartyName(CaseAssignedUserRolesResource userRoles, UserDetails userDetails,
                                         CaseData caseData) {
        String applicant1OrgCaseRole = caseData.getApplicant1OrganisationPolicy().getOrgPolicyCaseAssignedRole();
        String respondent1OrgCaseRole = caseData.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole();
        String applicant2OrgCaseRole = caseData.getApplicant2OrganisationPolicy() != null
                ? caseData.getApplicant2OrganisationPolicy().getOrgPolicyCaseAssignedRole() : EMPTY;
        String respondent2OrgCaseRole = caseData.getRespondent2OrganisationPolicy() != null
                ? caseData.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole() : EMPTY;

        Optional<CaseAssignedUserRole> applicantSol = userRoles.getCaseAssignedUserRoles().stream()
                .filter(CA -> CA.getUserId().equals(userDetails.getId())).findFirst();
        if (applicantSol.isPresent()) {
            CaseAssignedUserRole applicantSolicitor = applicantSol.get();
            if (applicant1OrgCaseRole.equals(applicantSolicitor.getCaseRole())) {
                return caseData.getApplicant1().getPartyName();
            }
            if (applicant2OrgCaseRole.equals(applicantSolicitor.getCaseRole())) {
                if (caseData.getApplicant2() != null) {
                    return caseData.getApplicant2().getPartyName();
                }
            }
            if (respondent1OrgCaseRole.equals(applicantSolicitor.getCaseRole())) {
                return caseData.getRespondent1().getPartyName();
            }
            if (respondent2OrgCaseRole.equals(applicantSolicitor.getCaseRole())) {
                if (caseData.getRespondent2() != null) {
                    return caseData.getRespondent2().getPartyName();
                }
            }
        }
        return EMPTY;
    }

    public String getCaaAccessToken() {
        return userService.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );
    }

}
