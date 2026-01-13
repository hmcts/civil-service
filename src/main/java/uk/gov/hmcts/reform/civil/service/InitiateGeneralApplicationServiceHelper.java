package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAParties;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;
import uk.gov.hmcts.reform.civil.utils.UserRoleUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils.getRespondent1SolicitorOrgId;
import static uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils.getRespondent2SolicitorOrgId;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitiateGeneralApplicationServiceHelper {

    private final CaseAssignmentApi caseAssignmentApi;
    private final UserRoleCaching userRoleCaching;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;
    private final WorkingDayIndicator workingDayIndicator;

    public static final String APPLICANT_ID = "001";
    public static final String RESPONDENT_ID = "002";
    public static final String RESPONDENT2_ID = "003";
    public static final String APPLICANT2_ID = "004";
    private static final int LIP_URGENT_DAYS = 10;
    private static final String LIP_URGENT_REASON = "There is a hearing on the main case within 10 days";

    public GeneralApplication setRespondentDetailsIfPresent(GeneralApplication generalApplication,
                                                            CaseData caseData, UserDetails userDetails,
                                                            GeneralAppFeesService feesService) {
        if (caseData.getApplicant1OrganisationPolicy() == null
            || caseData.getRespondent1OrganisationPolicy() == null
            || (YES.equals(caseData.getAddRespondent2()) && caseData.getRespondent2OrganisationPolicy() == null)) {
            throw new IllegalArgumentException("Solicitor Org details are not set correctly.");
        }

        String parentCaseId = caseData.getCcdCaseReference().toString();
        String applicant1OrgCaseRole = caseData.getApplicant1OrganisationPolicy().getOrgPolicyCaseAssignedRole();
        String respondent1OrgCaseRole = caseData.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole();

        CaseAssignmentUserRolesResource userRoles = getUserRoles(parentCaseId);

        /*
         * Set GA applicant solicitor details
         * */
        GASolicitorDetailsGAspec applicantBuilder = new GASolicitorDetailsGAspec()
            .setId(userDetails.getId())
            .setEmail(userDetails.getEmail())
            .setForename(userDetails.getForename())
            .setSurname(userDetails.getSurname());

        /*
         * Filter the case users to collect solicitors whose ID doesn't match with GA Applicant Solicitor's ID
         * There can be multiple applicant solicitors
         * */
        List<CaseAssignmentUserRole> caseAssignments = userRoles.getCaseAssignmentUserRoles();

        List<CaseAssignmentUserRole> applicantSolicitorList = Optional.ofNullable(caseAssignments)
            .orElse(Collections.emptyList())
            .stream()
            .filter(caseAssigned -> Objects.equals(
                caseAssigned.getUserId(),
                userDetails != null ? userDetails.getId() : null
            ))
            .toList();

        boolean sameDefSol1v2 = applicantSolicitorList.size() == 2
                && applicantSolicitorList.get(0).getUserId()
                .equals(applicantSolicitorList.get(1).getUserId());

        GeneralApplication.GeneralApplicationBuilder applicationBuilder = generalApplication.toBuilder();
        //only assign value if lip is applicant
        Boolean isGaAppSameAsParentCaseClLip = null;
        if (!CollectionUtils.isEmpty(applicantSolicitorList) && (applicantSolicitorList.size() == 1 || sameDefSol1v2)) {
            isGaAppSameAsParentCaseClLip = setSingleGaApplicant(applicantSolicitorList, applicationBuilder,
                    applicantBuilder,  applicant1OrgCaseRole, respondent1OrgCaseRole, caseData);
        }
        applicationBuilder
            .generalAppApplnSolicitor(applicantBuilder);

        List<String> gaApplicantRolesOnMainCase = applicantSolicitorList.stream()
            .map(CaseAssignmentUserRole::getCaseRole)
            .toList();

        List<CaseAssignmentUserRole> respondentSolicitors = Optional.ofNullable(caseAssignments)
            .orElse(Collections.emptyList())
            .stream()
            .filter(caseAssignedRoleEntry -> !Objects.equals(
                caseAssignedRoleEntry.getUserId(),
                userDetails.getId()
            ))
            .filter(caseAssignedRoleEntry ->
                        gaApplicantRolesOnMainCase.stream()
                            .noneMatch(applicantRole ->
                                           StringUtils.equalsIgnoreCase(applicantRole, caseAssignedRoleEntry.getCaseRole())
                            )
            )
            .toList();

        GAParties applicantPartyData;
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
                                                                                      applicantBuilder
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
                                           : NO).build();

        /*
        * Don't consider hearing date if application is represented
        * */
        if (Objects.nonNull(applicationBuilder.build().getIsGaApplicantLip())
            && applicationBuilder.build().getIsGaApplicantLip().equals(YES)) {
            checkLipUrgency(isGaAppSameAsParentCaseClLip, applicationBuilder, generalApplication, caseData, feesService);
        }

        return applicationBuilder.build();
    }

    private void checkLipUrgency(Boolean isGaAppSameAsParentCaseClLip,
                                 GeneralApplication.GeneralApplicationBuilder applicationBuilder,
                                 GeneralApplication generalApplication,
                                 CaseData caseData,
                                 GeneralAppFeesService feesService) {

        LocalDate startDate = LocalDateTime.now().getHour() >= 16
            ? LocalDate.now().plusDays(1)
            : LocalDate.now();

        LocalDate lipUrgentEndDate = LocalDate.now().plusDays(LIP_URGENT_DAYS);

        long noOfHoliday = startDate.datesUntil(lipUrgentEndDate)
            .filter(date -> !workingDayIndicator.isWorkingDay(date)).count();

        if (Objects.nonNull(isGaAppSameAsParentCaseClLip)
            && Objects.nonNull(caseData.getHearingDate())
            && caseData.getHearingDate().isBefore(lipUrgentEndDate.plusDays(noOfHoliday))) {

            GAUrgencyRequirement urgencyRequirement = new GAUrgencyRequirement();
            urgencyRequirement.setGeneralAppUrgency(YES);
            urgencyRequirement.setUrgentAppConsiderationDate(caseData.getHearingDate());
            urgencyRequirement.setReasonsForUrgency(LIP_URGENT_REASON);
            applicationBuilder.generalAppUrgencyRequirement(urgencyRequirement);
        } else if (caseData.isRespondent1LiP() || caseData.isRespondent2LiP() || caseData.isApplicantNotRepresented()) {
            GAUrgencyRequirement urgencyRequirement = new GAUrgencyRequirement();
            urgencyRequirement.setGeneralAppUrgency(NO);
            urgencyRequirement.setUrgentAppConsiderationDate(caseData.getHearingDate());
            applicationBuilder.generalAppUrgencyRequirement(urgencyRequirement);
        }
        //set main case hearing date as ga hearing date
        if (Objects.nonNull(isGaAppSameAsParentCaseClLip) && Objects.nonNull(caseData.getHearingDate())) {
            GAHearingDateGAspec hearingDate = new GAHearingDateGAspec();
            hearingDate.setHearingScheduledDate(caseData.getHearingDate());
            applicationBuilder.generalAppHearingDate(hearingDate);
            Fee feeForGA = feesService.getFeeForGA(generalApplication, caseData.getHearingDate());
            GAPbaDetails generalAppPBADetails = new GAPbaDetails();
            generalAppPBADetails.setFee(feeForGA);
            applicationBuilder.generalAppPBADetails(generalAppPBADetails);
        }
    }

    private Boolean setSingleGaApplicant(List<CaseAssignmentUserRole> applicantSolicitor,
                                      GeneralApplication.GeneralApplicationBuilder applicationBuilder,
                                      GASolicitorDetailsGAspec applicantBuilder,
                                      String applicant1OrgCaseRole,
                                      String respondent1OrgCaseRole,
                                      CaseData caseData) {
        CaseAssignmentUserRole applnSol = applicantSolicitor.get(0);
        Boolean isGaAppSameAsParentCaseClLip = null;
        if (applnSol.getCaseRole() != null) {
            if (applnSol.getCaseRole().equals(CaseRole.CLAIMANT.getFormattedName())
                    || applnSol.getCaseRole().equals(CaseRole.DEFENDANT.getFormattedName())) {

                applicationBuilder.isGaApplicantLip(YES);
                if (applnSol.getCaseRole().equals(CaseRole.DEFENDANT.getFormattedName())) {
                    isGaAppSameAsParentCaseClLip = false;
                } else {
                    isGaAppSameAsParentCaseClLip = true;
                }
            }
            if (applnSol.getCaseRole().equals(applicant1OrgCaseRole)) {

                applicantBuilder.setOrganisationIdentifier(caseData.getApplicant1OrganisationPolicy()
                        .getOrganisation().getOrganisationID());

            } else if (applnSol.getCaseRole().equals(respondent1OrgCaseRole)) {

                applicantBuilder.setOrganisationIdentifier(getRespondent1SolicitorOrgId(caseData));

            } else if (caseData.getAddRespondent2() != null && caseData.getAddRespondent2().equals(YES)
                    && applnSol.getCaseRole().equals(caseData.getRespondent2OrganisationPolicy()
                    .getOrgPolicyCaseAssignedRole())) {

                applicantBuilder.setOrganisationIdentifier(getRespondent2SolicitorOrgId(caseData));

            } else {
                if (caseData.getAddApplicant2() != null && caseData.getAddApplicant2().equals(YES)
                        && applnSol.getCaseRole().equals(caseData
                        .getApplicant2OrganisationPolicy()
                        .getOrgPolicyCaseAssignedRole())) {

                    applicantBuilder.setOrganisationIdentifier(caseData.getApplicant2OrganisationPolicy()
                            .getOrgPolicyCaseAssignedRole());

                }
            }
        }
        return isGaAppSameAsParentCaseClLip;
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

    private List<Element<GASolicitorDetailsGAspec>> collectGaRespondentSolicitors(List<CaseAssignmentUserRole> respondentSolicitors,
                                                                                  GeneralApplication.GeneralApplicationBuilder applicationBuilder,
                                                                                  CaseData caseData,
                                                                                  String applicant1OrgCaseRole,
                                                                                  String respondent1OrgCaseRole) {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        respondentSolicitors.forEach(respSol -> {
            GASolicitorDetailsGAspec specBuilder = new GASolicitorDetailsGAspec();

            if (respSol.getCaseRole() != null) {
                log.info(respSol.getCaseRole(), "**", respSol.getUserId());
                /*GA for Lips is only 1v1, check user id with ClaimantUserDetails/DefendantUserDetails*/
                if (respSol.getCaseRole().equals(CaseRole.CLAIMANT.getFormattedName())
                        || respSol.getCaseRole().equals(CaseRole.DEFENDANT.getFormattedName())) {
                    applicationBuilder.isGaRespondentOneLip(YES);
                    specBuilder.setId(respSol.getUserId());
                    if (Objects.nonNull(caseData.getDefendantUserDetails())
                            && respSol.getUserId().equals(caseData.getDefendantUserDetails().getId())) {
                        specBuilder.setEmail(caseData.getDefendantUserDetails().getEmail());
                        specBuilder.setForename(caseData.getRespondent1().getIndividualFirstName());
                        if (Objects.nonNull(caseData.getRespondent1().getIndividualLastName())) {
                            specBuilder.setSurname(Optional.of(caseData.getRespondent1().getIndividualLastName()));
                        } else {
                            specBuilder.setSurname(Optional.empty());
                        }
                    } else if (Objects.nonNull(caseData.getClaimantUserDetails())
                            && respSol.getUserId().equals(caseData.getClaimantUserDetails().getId())) {
                        specBuilder.setEmail(caseData.getClaimantUserDetails().getEmail());
                        specBuilder.setForename(caseData.getApplicant1().getIndividualFirstName());
                        if (Objects.nonNull(caseData.getApplicant1().getIndividualLastName())) {
                            specBuilder.setSurname(Optional.of(caseData.getApplicant1().getIndividualLastName()));
                        } else {
                            specBuilder.setSurname(Optional.empty());
                        }
                    }
                    /*Populate the GA respondent solicitor details in accordance with civil case Applicant Solicitor 1
                details if case role of collected user matches with case role of Applicant 1*/
                } else if (respSol.getCaseRole().equals(applicant1OrgCaseRole)) {
                    if (caseData.getApplicantSolicitor1UserDetails() != null) {
                        specBuilder.setId(respSol.getUserId());
                        specBuilder.setEmail(caseData.getApplicantSolicitor1UserDetails().getEmail());
                        specBuilder.setOrganisationIdentifier(caseData.getApplicant1OrganisationPolicy()
                                .getOrganisation().getOrganisationID());
                    }
                /*Populate the GA respondent solicitor details in accordance with civil case Respondent
                Solicitor 1 details if caserole of collected user matches with caserole Respondent Solicitor 1*/
                } else if (respSol.getCaseRole().equals(respondent1OrgCaseRole)) {
                    specBuilder.setId(respSol.getUserId());
                    specBuilder.setEmail(caseData.getRespondentSolicitor1EmailAddress());
                    specBuilder.setOrganisationIdentifier(getRespondent1SolicitorOrgId(caseData));

                /*Populate the GA respondent solicitor details in accordance with civil case Respondent
                Solicitor 2 details if it's 1 V 2 Different Solicitor scenario*/
                } else {
                    if (Objects.nonNull(caseData.getAddRespondent2())
                            && caseData.getAddRespondent2().equals(YES)) {
                        specBuilder.setId(respSol.getUserId());
                        specBuilder.setEmail(caseData.getRespondentSolicitor2EmailAddress());
                        specBuilder.setOrganisationIdentifier(getRespondent2SolicitorOrgId(caseData));
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

            GASolicitorDetailsGAspec gaSolicitorDetailsGAspec = specBuilder;
            if (Objects.nonNull(gaSolicitorDetailsGAspec.getId())) {
                respondentSols.add(element(gaSolicitorDetailsGAspec));
            }

        });
        return respondentSols;
    }

    private GAParties getApplicantPartyData(CaseAssignmentUserRolesResource userRoles, UserDetails userDetails,
                                            CaseData caseData) {
        String applicant1OrgCaseRole = caseData.getApplicant1OrganisationPolicy().getOrgPolicyCaseAssignedRole();
        String respondent1OrgCaseRole = caseData.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole();
        String applicant2OrgCaseRole = caseData.getApplicant2OrganisationPolicy() != null
            ? caseData.getApplicant2OrganisationPolicy().getOrgPolicyCaseAssignedRole() : EMPTY;
        String respondent2OrgCaseRole = caseData.getRespondent2OrganisationPolicy() != null
            ? caseData.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole() : EMPTY;

        Optional<CaseAssignmentUserRole> applicantSol = userRoles.getCaseAssignmentUserRoles().stream()
            .filter(caseAssigned -> caseAssigned.getUserId().equals(userDetails.getId())).findFirst();
        if (applicantSol.isPresent()) {
            CaseAssignmentUserRole applicantSolicitor = applicantSol.get();
            /*GA for Lips is only 1v1*/
            if (applicant1OrgCaseRole.equals(applicantSolicitor.getCaseRole())
                || applicantSolicitor.getCaseRole().equals(CaseRole.CLAIMANT.getFormattedName())) {
                return new GAParties()
                    .setApplicantPartyName(caseData.getApplicant1().getPartyName())
                    .setLitigiousPartyID(APPLICANT_ID);
            }
            if (applicant2OrgCaseRole.equals(applicantSolicitor.getCaseRole()) && (caseData.getApplicant2() != null)) {
                return new GAParties()
                        .setApplicantPartyName(caseData.getApplicant2().getPartyName())
                        .setLitigiousPartyID(APPLICANT2_ID);
            }
            /*GA for Lips is only 1v1*/
            if (respondent1OrgCaseRole.equals(applicantSolicitor.getCaseRole())
                    || applicantSolicitor.getCaseRole().equals(CaseRole.DEFENDANT.getFormattedName())) {
                return new GAParties()
                    .setApplicantPartyName(caseData.getRespondent1().getPartyName())
                    .setLitigiousPartyID(RESPONDENT_ID);
            }
            if (respondent2OrgCaseRole.equals(applicantSolicitor.getCaseRole()) && caseData.getRespondent2() != null) {
                return new GAParties()
                        .setApplicantPartyName(caseData.getRespondent2().getPartyName())
                        .setLitigiousPartyID(RESPONDENT2_ID);
            }
        }
        return new GAParties();
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

    public CaseAssignmentUserRolesResource getUserRoles(String parentCaseId) {
        CaseAssignmentUserRolesResource userRoles = caseAssignmentApi.getUserRoles(
            getCaaAccessToken(), authTokenGenerator.generate(), List.of(parentCaseId));
        log.info("UserRoles from API: {}", userRoles);
        return userRoles;
    }

    public String getCaaAccessToken() {
        return userService.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );
    }

}

