package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAParties;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;
import uk.gov.hmcts.reform.civil.utils.UserRoleUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
@SuppressWarnings("unchecked")
public class InitiateGeneralApplicationServiceHelper {

    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final UserRoleCaching userRoleCaching;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;
    public static final String APPLICANT_ID = "001";
    public static final String RESPONDENT_ID = "002";
    public static final String RESPONDENT2_ID = "003";
    public static final String APPLICANT2_ID = "004";
    private static final int LIP_URGENT_DAYS = 10;
    private static final String LIP_URGENT_REASON = "There is a hearing on the main case within 10 days";

    public GeneralApplication setRespondentDetailsIfPresent(GeneralApplication generalApplication,
                                                            CaseData caseData, UserDetails userDetails) {
        if (caseData.getApplicant1OrganisationPolicy() == null
                || caseData.getRespondent1OrganisationPolicy() == null
                || (YES.equals(caseData.getAddRespondent2()) && caseData.getRespondent2OrganisationPolicy() == null)) {
            throw new IllegalArgumentException("Solicitor Org details are not set correctly.");
        }

        String parentCaseId = caseData.getCcdCaseReference().toString();
        String applicant1OrgCaseRole = caseData.getApplicant1OrganisationPolicy().getOrgPolicyCaseAssignedRole();
        String respondent1OrgCaseRole = caseData.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole();

        CaseAssignedUserRolesResource userRoles = getUserRoles(parentCaseId);

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
        boolean sameDefSol1v2 = applicantSolicitor.size() == 2
                && applicantSolicitor.get(0).getUserId()
                .equals(applicantSolicitor.get(1).getUserId());

        GeneralApplication.GeneralApplicationBuilder applicationBuilder = generalApplication.toBuilder();
        //only assign value if lip is applicant
        Boolean isGaAppSameAsParentCaseClLip = null;
        if (!CollectionUtils.isEmpty(applicantSolicitor) && (applicantSolicitor.size() == 1 || sameDefSol1v2)) {
            isGaAppSameAsParentCaseClLip = setSingleGaApplicant(applicantSolicitor, applicationBuilder,
                    applicantBuilder,  applicant1OrgCaseRole, respondent1OrgCaseRole, caseData);
        }
        applicationBuilder
            .generalAppApplnSolicitor(applicantBuilder.build());
        GAParties applicantPartyData = GAParties.builder().build();
        /*
         * Set GA respondent solicitors' details
         * */
        if (!CollectionUtils.isEmpty(respondentSolicitors)) {
            applicationBuilder.generalAppRespondentSolicitors(collectGaRespondentSolicitors(respondentSolicitors,
                    applicationBuilder, caseData, applicant1OrgCaseRole, respondent1OrgCaseRole));
        }

        applicantPartyData = getApplicantPartyData(userRoles, userDetails, caseData);
        applicationBuilder.applicantPartyName(applicantPartyData.getApplicantPartyName());
        applicationBuilder.litigiousPartyID(applicantPartyData.getLitigiousPartyID());
        boolean isGAApplicantSameAsParentCaseClaimant = isGAApplicantSameAsPCClaimant(caseData,
                                                                                      applicantBuilder.build()
                                                                                          .getOrganisationIdentifier(),
                isGaAppSameAsParentCaseClLip);
        String gaApplicantDisplayName;
        if (isGAApplicantSameAsParentCaseClaimant) {
            gaApplicantDisplayName = applicantPartyData.getApplicantPartyName() + " - Claimant";
        } else {
            gaApplicantDisplayName = applicantPartyData.getApplicantPartyName() + " - Defendant";
        }
        applicationBuilder.gaApplicantDisplayName(gaApplicantDisplayName);
        applicationBuilder
            .parentClaimantIsApplicant(isGAApplicantSameAsParentCaseClaimant
                                           ? YES
                                           : YesOrNo.NO).build();
        checkLipUrgency(isGaAppSameAsParentCaseClLip, applicationBuilder, caseData);
        return applicationBuilder.build();
    }

    private void checkLipUrgency(Boolean isGaAppSameAsParentCaseClLip,
                                 GeneralApplication.GeneralApplicationBuilder applicationBuilder,
                                 CaseData caseData) {
        if (Objects.nonNull(isGaAppSameAsParentCaseClLip)
                && Objects.nonNull(caseData.getHearingDate())
                && LocalDate.now().plusDays(LIP_URGENT_DAYS + 1).isAfter(caseData.getHearingDate())) {
            applicationBuilder.generalAppUrgencyRequirement(
                    GAUrgencyRequirement
                            .builder()
                            .generalAppUrgency(YES)
                            .urgentAppConsiderationDate(caseData.getHearingDate())
                            .reasonsForUrgency(LIP_URGENT_REASON).build());
        }
    }

    private Boolean setSingleGaApplicant(List<CaseAssignedUserRole> applicantSolicitor,
                                      GeneralApplication.GeneralApplicationBuilder applicationBuilder,
                                      GASolicitorDetailsGAspec.GASolicitorDetailsGAspecBuilder applicantBuilder,
                                      String applicant1OrgCaseRole,
                                      String respondent1OrgCaseRole,
                                      CaseData caseData) {
        CaseAssignedUserRole applnSol = applicantSolicitor.get(0);
        Boolean isGaAppSameAsParentCaseClLip = null;
        if (applnSol.getCaseRole() != null) {
            isGaAppSameAsParentCaseClLip = setGaLipApp(applnSol, applicationBuilder);
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
        return isGaAppSameAsParentCaseClLip;
    }

    private Boolean setGaLipApp(CaseAssignedUserRole applnSol,
                             GeneralApplication.GeneralApplicationBuilder applicationBuilder) {
        Boolean isGaAppSameAsParentCaseClLip = null;
        if (applnSol.getCaseRole().equals(CaseRole.CLAIMANT.getFormattedName())
                || applnSol.getCaseRole().equals(CaseRole.DEFENDANT.getFormattedName())) {
            applicationBuilder.isGaApplicantLip(YES);
            if (applnSol.getCaseRole().equals(CaseRole.DEFENDANT.getFormattedName())) {
                return false;
            } else {
                return true;
            }
        }
        return null;
    }

    private boolean isGAApplicantSameAsPCClaimant(CaseData caseData, String organisationIdentifier, Boolean isGAAppSameAsParentCaseLip) {
        if (Objects.nonNull(isGAAppSameAsParentCaseLip)) {
            return isGAAppSameAsParentCaseLip;
        } else {
            return caseData.getApplicantSolicitor1UserDetails() != null
                    && caseData.getApplicant1OrganisationPolicy() != null
                    && caseData.getApplicant1OrganisationPolicy().getOrganisation() != null
                    && caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID()
                    .equals(organisationIdentifier);
        }
    }

    private List<Element<GASolicitorDetailsGAspec>> collectGaRespondentSolicitors(List<CaseAssignedUserRole> respondentSolicitors,
                                                                                  GeneralApplication.GeneralApplicationBuilder applicationBuilder,
                                                                                  CaseData caseData,
                                                                                  String applicant1OrgCaseRole,
                                                                                  String respondent1OrgCaseRole) {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        respondentSolicitors.forEach((respSol) -> {
            GASolicitorDetailsGAspec.GASolicitorDetailsGAspecBuilder specBuilder = GASolicitorDetailsGAspec
                    .builder();

            if (respSol.getCaseRole() != null) {
                log.info(respSol.getCaseRole(), "**", respSol.getUserId());
                /*GA for Lips is only 1v1, check user id with ClaimantUserDetails/DefendantUserDetails*/
                if (respSol.getCaseRole().equals(CaseRole.CLAIMANT.getFormattedName())
                        || respSol.getCaseRole().equals(CaseRole.DEFENDANT.getFormattedName())) {
                    collectGaRespLip(applicationBuilder, specBuilder, respSol, caseData);
                    /*Populate the GA respondent solicitor details in accordance with civil case Applicant Solicitor 1
                details if case role of collected user matches with case role of Applicant 1*/
                } else if (respSol.getCaseRole().equals(applicant1OrgCaseRole)) {
                    if (caseData.getApplicantSolicitor1UserDetails() != null) {
                        specBuilder.id(respSol.getUserId());
                        specBuilder.email(caseData.getApplicantSolicitor1UserDetails().getEmail());
                        specBuilder.organisationIdentifier(caseData.getApplicant1OrganisationPolicy()
                                .getOrganisation().getOrganisationID());
                    }
                /*Populate the GA respondent solicitor details in accordance with civil case Respondent
                Solicitor 1 details if caserole of collected user matches with caserole Respondent Solicitor 1*/
                } else if (respSol.getCaseRole().equals(respondent1OrgCaseRole)) {
                    specBuilder.id(respSol.getUserId());
                    specBuilder.email(caseData.getRespondentSolicitor1EmailAddress());
                    specBuilder.organisationIdentifier(getRespondent1SolicitorOrgId(caseData));

                /*Populate the GA respondent solicitor details in accordance with civil case Respondent
                Solicitor 2 details if it's 1 V 2 Different Solicitor scenario*/
                } else {
                    if (Objects.nonNull(caseData.getAddRespondent2())
                            && caseData.getAddRespondent2().equals(YES)) {
                        specBuilder.id(respSol.getUserId());
                        specBuilder.email(caseData.getRespondentSolicitor2EmailAddress());
                        specBuilder.organisationIdentifier(getRespondent2SolicitorOrgId(caseData));
                    }
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
            if (Objects.nonNull(gaSolicitorDetailsGAspec.getId())) {
                respondentSols.add(element(gaSolicitorDetailsGAspec));
            }

        });
        return respondentSols;
    }

    private void collectGaRespLip(GeneralApplication.GeneralApplicationBuilder applicationBuilder,
                                  GASolicitorDetailsGAspec.GASolicitorDetailsGAspecBuilder specBuilder,
                                  CaseAssignedUserRole respSol,
                                  CaseData caseData) {
        applicationBuilder.isGaRespondentOneLip(YES);
        specBuilder.id(respSol.getUserId());
        if (Objects.nonNull(caseData.getDefendantUserDetails())
                && respSol.getUserId().equals(caseData.getDefendantUserDetails().getId())) {
            specBuilder.email(caseData.getDefendantUserDetails().getEmail());
            specBuilder.forename(caseData.getRespondent1().getIndividualFirstName());
            if (Objects.nonNull(caseData.getRespondent1().getIndividualLastName())) {
                specBuilder.surname(Optional.of(caseData.getRespondent1().getIndividualLastName()));
            } else {
                specBuilder.surname(Optional.empty());
            }
        } else if (Objects.nonNull(caseData.getClaimantUserDetails())
                && respSol.getUserId().equals(caseData.getClaimantUserDetails().getId())) {
            specBuilder.email(caseData.getClaimantUserDetails().getEmail());
            specBuilder.forename(caseData.getApplicant1().getIndividualFirstName());
            if (Objects.nonNull(caseData.getApplicant1().getIndividualLastName())) {
                specBuilder.surname(Optional.of(caseData.getApplicant1().getIndividualLastName()));
            } else {
                specBuilder.surname(Optional.empty());
            }
        }
    }

    private GAParties getApplicantPartyData(CaseAssignedUserRolesResource userRoles, UserDetails userDetails,
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
            /*GA for Lips is only 1v1*/
            if (applicant1OrgCaseRole.equals(applicantSolicitor.getCaseRole())
                || applicantSolicitor.getCaseRole().equals(CaseRole.CLAIMANT.getFormattedName())) {
                return GAParties.builder()
                        .applicantPartyName(caseData.getApplicant1().getPartyName())
                        .litigiousPartyID(APPLICANT_ID)
                        .build();
            }
            if (applicant2OrgCaseRole.equals(applicantSolicitor.getCaseRole())) {
                if (caseData.getApplicant2() != null) {
                    return GAParties.builder()
                            .applicantPartyName(caseData.getApplicant2().getPartyName())
                            .litigiousPartyID(APPLICANT2_ID)
                            .build();
                }
            }
            /*GA for Lips is only 1v1*/
            if (respondent1OrgCaseRole.equals(applicantSolicitor.getCaseRole())
                    || applicantSolicitor.getCaseRole().equals(CaseRole.DEFENDANT.getFormattedName())) {
                return GAParties.builder()
                        .applicantPartyName(caseData.getRespondent1().getPartyName())
                        .litigiousPartyID(RESPONDENT_ID)
                        .build();
            }
            if (respondent2OrgCaseRole.equals(applicantSolicitor.getCaseRole())) {
                if (caseData.getRespondent2() != null) {
                    return GAParties.builder()
                            .applicantPartyName(caseData.getRespondent2().getPartyName())
                            .litigiousPartyID(RESPONDENT2_ID)
                            .build();
                }
            }
        }
        return GAParties.builder().build();
    }

    public boolean isGAApplicantSameAsParentCaseClaimant(CaseData caseData, String authToken) {
        String parentCaseId = caseData.getCcdCaseReference().toString();
        List<String> userRolesCaching = userRoleCaching.getUserRoles(authToken, parentCaseId);

        boolean isApplicantSolicitor = UserRoleUtils.isApplicantSolicitor(userRolesCaching);

        String applicant1OrgCaseRole = caseData.getApplicant1OrganisationPolicy().getOrgPolicyCaseAssignedRole();

        if (!CollectionUtils.isEmpty(userRolesCaching) && userRolesCaching.size() == 1 && isApplicantSolicitor) {

            String applnSol = userRolesCaching.get(0);

            if (applnSol != null && applnSol.equals(applicant1OrgCaseRole)) {
                return true;
            }
        }

        return false;
    }

    public CaseAssignedUserRolesResource getUserRoles(String parentCaseId) {
        CaseAssignedUserRolesResource userRoles = caseAccessDataStoreApi.getUserRoles(
                getCaaAccessToken(), authTokenGenerator.generate(), List.of(parentCaseId));
        log.info("UserRoles from API :" + userRoles);
        return userRoles;
    }

    public String getCaaAccessToken() {
        return userService.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );
    }

}
