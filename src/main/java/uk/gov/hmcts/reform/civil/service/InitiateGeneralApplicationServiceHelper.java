package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
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
        validateOrganisationPolicies(caseData);
        String parentCaseId = caseData.getCcdCaseReference().toString();
        String applicant1OrgCaseRole = caseData.getApplicant1OrganisationPolicy().getOrgPolicyCaseAssignedRole();
        String respondent1OrgCaseRole = caseData.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole();
        CaseAssignmentUserRolesResource userRoles = getUserRoles(parentCaseId);
        List<CaseAssignmentUserRole> caseAssignments = Optional.ofNullable(userRoles.getCaseAssignmentUserRoles())
            .orElse(Collections.emptyList());
        GASolicitorDetailsGAspec applicantBuilder = buildApplicantSolicitorDetails(userDetails);
        List<CaseAssignmentUserRole> applicantSolicitorList = getApplicantSolicitorAssignments(caseAssignments, userDetails.getId());

        GeneralApplication applicationBuilder = generalApplication.copy();
        final Optional<Boolean> isGaAppSameAsParentCaseClLip = populatePrimaryApplicantSolicitorDetails(
            applicantSolicitorList,
            applicationBuilder,
            applicantBuilder,
            applicant1OrgCaseRole,
            respondent1OrgCaseRole,
            caseData
        );
        applicationBuilder.setGeneralAppApplnSolicitor(applicantBuilder);

        SolicitorAssignments solicitorAssignments = splitSolicitorAssignments(caseAssignments, userDetails.getId(), applicantSolicitorList);
        applicationBuilder.setGeneralAppApplicantAddlSolicitors(collectGaSolicitors(
            solicitorAssignments.applicantAdditionalSolicitors(),
            applicationBuilder,
            caseData,
            applicant1OrgCaseRole,
            respondent1OrgCaseRole
        ));
        setRespondentSolicitorDetails(
            applicationBuilder,
            solicitorAssignments.respondentSolicitors(),
            caseData,
            applicant1OrgCaseRole,
            respondent1OrgCaseRole
        );

        GAParties applicantPartyData = getApplicantPartyData(userRoles, userDetails, caseData);
        populateApplicantPartyFields(applicationBuilder, applicantPartyData, caseData,
            applicantBuilder.getOrganisationIdentifier(), isGaAppSameAsParentCaseClLip);
        if (YES.equals(applicationBuilder.getIsGaApplicantLip())) {
            checkLipUrgency(isGaAppSameAsParentCaseClLip, applicationBuilder, generalApplication, caseData, feesService);
        }

        return applicationBuilder;
    }

    private void validateOrganisationPolicies(CaseData caseData) {
        if (caseData.getApplicant1OrganisationPolicy() == null
            || caseData.getRespondent1OrganisationPolicy() == null
            || (YES.equals(caseData.getAddRespondent2()) && caseData.getRespondent2OrganisationPolicy() == null)) {
            throw new IllegalArgumentException("Solicitor Org details are not set correctly.");
        }
    }

    private GASolicitorDetailsGAspec buildApplicantSolicitorDetails(UserDetails userDetails) {
        return new GASolicitorDetailsGAspec()
            .setId(userDetails.getId())
            .setEmail(userDetails.getEmail())
            .setForename(userDetails.getForename())
            .setSurname(userDetails.getSurname());
    }

    private List<CaseAssignmentUserRole> getApplicantSolicitorAssignments(List<CaseAssignmentUserRole> caseAssignments,
                                                                          String userId) {
        return caseAssignments.stream()
            .filter(caseAssigned -> Objects.equals(caseAssigned.getUserId(), userId))
            .toList();
    }

    private Optional<Boolean> populatePrimaryApplicantSolicitorDetails(List<CaseAssignmentUserRole> applicantSolicitorList,
                                                                       GeneralApplication applicationBuilder,
                                                                       GASolicitorDetailsGAspec applicantBuilder,
                                                                       String applicant1OrgCaseRole,
                                                                       String respondent1OrgCaseRole,
                                                                       CaseData caseData) {
        if (CollectionUtils.isEmpty(applicantSolicitorList)
            || (!hasSingleApplicantAssignment(applicantSolicitorList) && applicantSolicitorList.size() != 1)) {
            return Optional.empty();
        }
        return setSingleGaApplicant(
            applicantSolicitorList,
            applicationBuilder,
            applicantBuilder,
            applicant1OrgCaseRole,
            respondent1OrgCaseRole,
            caseData
        );
    }

    private boolean hasSingleApplicantAssignment(List<CaseAssignmentUserRole> applicantSolicitorList) {
        return applicantSolicitorList.size() == 2
            && applicantSolicitorList.get(0).getUserId().equals(applicantSolicitorList.get(1).getUserId());
    }

    private SolicitorAssignments splitSolicitorAssignments(List<CaseAssignmentUserRole> caseAssignments,
                                                           String currentUserId,
                                                           List<CaseAssignmentUserRole> applicantSolicitorList) {
        List<String> gaApplicantRolesOnMainCase = applicantSolicitorList.stream()
            .map(CaseAssignmentUserRole::getCaseRole)
            .toList();
        List<CaseAssignmentUserRole> gaApplicantAddlnSolicitors = new ArrayList<>();
        List<CaseAssignmentUserRole> respondentSolicitors = new ArrayList<>();
        for (CaseAssignmentUserRole entry : caseAssignments) {
            if (Objects.equals(entry.getUserId(), currentUserId)) {
                continue;
            }
            String role = entry.getCaseRole();
            if (role != null && gaApplicantRolesOnMainCase.contains(role)) {
                gaApplicantAddlnSolicitors.add(entry);
            } else {
                respondentSolicitors.add(entry);
            }
        }
        return new SolicitorAssignments(gaApplicantAddlnSolicitors, respondentSolicitors);
    }

    private void setRespondentSolicitorDetails(GeneralApplication applicationBuilder,
                                               List<CaseAssignmentUserRole> respondentSolicitors,
                                               CaseData caseData,
                                               String applicant1OrgCaseRole,
                                               String respondent1OrgCaseRole) {
        if (CollectionUtils.isEmpty(respondentSolicitors)) {
            return;
        }
        applicationBuilder.setGeneralAppRespondentSolicitors(collectGaSolicitors(
            respondentSolicitors,
            applicationBuilder,
            caseData,
            applicant1OrgCaseRole,
            respondent1OrgCaseRole
        ));
    }

    private void populateApplicantPartyFields(GeneralApplication applicationBuilder,
                                              GAParties applicantPartyData,
                                              CaseData caseData,
                                              String organisationIdentifier,
                                              Optional<Boolean> isGaAppSameAsParentCaseClLip) {
        applicationBuilder.setApplicantPartyName(applicantPartyData.getApplicantPartyName());
        applicationBuilder.setLitigiousPartyID(applicantPartyData.getLitigiousPartyID());
        boolean isGAApplicantSameAsParentCaseClaimant = isGAApplicantSameAsPCClaimant(
            caseData,
            organisationIdentifier,
            isGaAppSameAsParentCaseClLip
        );
        applicationBuilder.setGaApplicantDisplayName(buildGaApplicantDisplayName(
            applicantPartyData.getApplicantPartyName(),
            isGAApplicantSameAsParentCaseClaimant
        ));
        applicationBuilder.setParentClaimantIsApplicant(isGAApplicantSameAsParentCaseClaimant ? YES : NO);
    }

    private String buildGaApplicantDisplayName(String applicantPartyName, boolean isGAApplicantSameAsParentCaseClaimant) {
        return applicantPartyName + (isGAApplicantSameAsParentCaseClaimant ? " - Claimant" : " - Defendant");
    }

    private void checkLipUrgency(Optional<Boolean> isGaAppSameAsParentCaseClLip,
                                 GeneralApplication applicationBuilder,
                                 GeneralApplication generalApplication,
                                 CaseData caseData,
                                 GeneralAppFeesService feesService) {

        LocalDate startDate = LocalDateTime.now().getHour() >= 16
            ? LocalDate.now().plusDays(1)
            : LocalDate.now();

        LocalDate lipUrgentEndDate = LocalDate.now().plusDays(LIP_URGENT_DAYS);

        long noOfHoliday = startDate.datesUntil(lipUrgentEndDate)
            .filter(date -> !workingDayIndicator.isWorkingDay(date)).count();

        if (isGaAppSameAsParentCaseClLip.isPresent()
            && Objects.nonNull(caseData.getHearingDate())
            && caseData.getHearingDate().isBefore(lipUrgentEndDate.plusDays(noOfHoliday))) {

            GAUrgencyRequirement urgencyRequirement = new GAUrgencyRequirement();
            urgencyRequirement.setGeneralAppUrgency(YES);
            urgencyRequirement.setUrgentAppConsiderationDate(caseData.getHearingDate());
            urgencyRequirement.setReasonsForUrgency(LIP_URGENT_REASON);
            applicationBuilder.setGeneralAppUrgencyRequirement(urgencyRequirement);
        } else if (caseData.isRespondent1LiP() || caseData.isRespondent2LiP() || caseData.isApplicantNotRepresented()) {
            GAUrgencyRequirement urgencyRequirement = new GAUrgencyRequirement();
            urgencyRequirement.setGeneralAppUrgency(NO);
            urgencyRequirement.setUrgentAppConsiderationDate(caseData.getHearingDate());
            applicationBuilder.setGeneralAppUrgencyRequirement(urgencyRequirement);
        }
        //set main case hearing date as ga hearing date
        if (isGaAppSameAsParentCaseClLip.isPresent() && Objects.nonNull(caseData.getHearingDate())) {
            GAHearingDateGAspec hearingDate = new GAHearingDateGAspec();
            hearingDate.setHearingScheduledDate(caseData.getHearingDate());
            applicationBuilder.setGeneralAppHearingDate(hearingDate);
            Fee feeForGA = feesService.getFeeForGA(generalApplication, caseData.getHearingDate());
            GAPbaDetails generalAppPBADetails = new GAPbaDetails();
            generalAppPBADetails.setFee(feeForGA);
            applicationBuilder.setGeneralAppPBADetails(generalAppPBADetails);
        }
    }

    private Optional<Boolean> setSingleGaApplicant(List<CaseAssignmentUserRole> applicantSolicitor,
                                                   GeneralApplication applicationBuilder,
                                                   GASolicitorDetailsGAspec applicantBuilder,
                                                   String applicant1OrgCaseRole,
                                                   String respondent1OrgCaseRole,
                                                   CaseData caseData) {
        String caseRole = applicantSolicitor.getFirst().getCaseRole();
        if (caseRole == null) {
            return Optional.empty();
        }
        Optional<Boolean> isGaAppSameAsParentCaseClLip = resolveLipApplicant(caseRole, applicationBuilder);
        String organisationIdentifier = resolveApplicantOrganisationIdentifier(
            caseRole,
            applicant1OrgCaseRole,
            respondent1OrgCaseRole,
            caseData
        );
        if (organisationIdentifier != null) {
            applicantBuilder.setOrganisationIdentifier(organisationIdentifier);
        }
        return isGaAppSameAsParentCaseClLip;
    }

    private Optional<Boolean> resolveLipApplicant(String caseRole, GeneralApplication applicationBuilder) {
        if (!isLipCaseRole(caseRole)) {
            return Optional.empty();
        }
        applicationBuilder.setIsGaApplicantLip(YES);
        return Optional.of(!CaseRole.DEFENDANT.getFormattedName().equals(caseRole));
    }

    private String resolveApplicantOrganisationIdentifier(String caseRole,
                                                          String applicant1OrgCaseRole,
                                                          String respondent1OrgCaseRole,
                                                          CaseData caseData) {
        if (caseRole.equals(applicant1OrgCaseRole)) {
            return caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        }
        if (caseRole.equals(respondent1OrgCaseRole)) {
            return getRespondent1SolicitorOrgId(caseData);
        }
        if (matchesRespondent2Solicitor(caseData, caseRole)) {
            return getRespondent2SolicitorOrgId(caseData);
        }
        if (matchesApplicant2Solicitor(caseData, caseRole)) {
            return caseData.getApplicant2OrganisationPolicy().getOrgPolicyCaseAssignedRole();
        }
        return null;
    }

    private boolean matchesRespondent2Solicitor(CaseData caseData, String caseRole) {
        return YES.equals(caseData.getAddRespondent2())
            && caseRole.equals(caseData.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole());
    }

    private boolean matchesApplicant2Solicitor(CaseData caseData, String caseRole) {
        return YES.equals(caseData.getAddApplicant2())
            && caseRole.equals(caseData.getApplicant2OrganisationPolicy().getOrgPolicyCaseAssignedRole());
    }

    private boolean isGAApplicantSameAsPCClaimant(CaseData caseData,
                                                  String organisationIdentifier,
                                                  Optional<Boolean> isGAAppSameAsParentCaseLip) {
        return isGAAppSameAsParentCaseLip.orElseGet(() -> caseData.getApplicantSolicitor1UserDetails() != null
                && caseData.getApplicant1OrganisationPolicy() != null
                && caseData.getApplicant1OrganisationPolicy().getOrganisation() != null
                && caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID()
                .equals(organisationIdentifier));
    }

    private List<Element<GASolicitorDetailsGAspec>> collectGaSolicitors(List<CaseAssignmentUserRole> solicitors,
                                                                                  GeneralApplication applicationBuilder,
                                                                                  CaseData caseData,
                                                                                  String applicant1OrgCaseRole,
                                                                                  String respondent1OrgCaseRole) {
        List<Element<GASolicitorDetailsGAspec>> addedSols = new ArrayList<>();
        solicitors.forEach(sol -> addSolicitorIfPresent(
            addedSols,
            buildGaSolicitorDetails(sol, applicationBuilder, caseData, applicant1OrgCaseRole, respondent1OrgCaseRole)
        ));
        return addedSols;
    }

    private GASolicitorDetailsGAspec buildGaSolicitorDetails(CaseAssignmentUserRole solicitor,
                                                             GeneralApplication applicationBuilder,
                                                             CaseData caseData,
                                                             String applicant1OrgCaseRole,
                                                             String respondent1OrgCaseRole) {
        GASolicitorDetailsGAspec gaSolicitorDetailsGAspec = new GASolicitorDetailsGAspec();
        String caseRole = getCaseRoleOrThrow(solicitor);
        if (isLipCaseRole(caseRole)) {
            populateLipSolicitorDetails(gaSolicitorDetailsGAspec, solicitor, applicationBuilder, caseData);
        } else if (caseRole.equals(applicant1OrgCaseRole)) {
            populateApplicantSolicitorDetails(gaSolicitorDetailsGAspec, solicitor, caseData);
        } else if (caseRole.equals(respondent1OrgCaseRole)) {
            populateRespondentOneSolicitorDetails(gaSolicitorDetailsGAspec, solicitor, caseData);
        } else {
            populateRespondentTwoSolicitorDetails(gaSolicitorDetailsGAspec, solicitor, caseData);
        }
        return gaSolicitorDetailsGAspec;
    }

    private String getCaseRoleOrThrow(CaseAssignmentUserRole solicitor) {
        if (solicitor.getCaseRole() == null) {
            String errorMsg = String.format("Invalid User (userId [%s]): Without Case Role ", solicitor.getUserId());
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        log.info("{} ** {}", solicitor.getCaseRole(), solicitor.getUserId());
        return solicitor.getCaseRole();
    }

    private boolean isLipCaseRole(String caseRole) {
        return CaseRole.CLAIMANT.getFormattedName().equals(caseRole)
            || CaseRole.DEFENDANT.getFormattedName().equals(caseRole);
    }

    private void populateLipSolicitorDetails(GASolicitorDetailsGAspec gaSolicitorDetailsGAspec,
                                             CaseAssignmentUserRole solicitor,
                                             GeneralApplication applicationBuilder,
                                             CaseData caseData) {
        applicationBuilder.setIsGaRespondentOneLip(YES);
        gaSolicitorDetailsGAspec.setId(solicitor.getUserId());
        if (setLipSolicitorDetailsIfPresent(
            gaSolicitorDetailsGAspec,
            solicitor.getUserId(),
            caseData.getDefendantUserDetails(),
            caseData.getRespondent1()
        )) {
            return;
        }
        setLipSolicitorDetailsIfPresent(
            gaSolicitorDetailsGAspec,
            solicitor.getUserId(),
            caseData.getClaimantUserDetails(),
            caseData.getApplicant1()
        );
    }

    private boolean setLipSolicitorDetailsIfPresent(GASolicitorDetailsGAspec gaSolicitorDetailsGAspec,
                                                    String userId,
                                                    IdamUserDetails idamUserDetails,
                                                    Party party) {
        if (Objects.isNull(idamUserDetails) || !userId.equals(idamUserDetails.getId())) {
            return false;
        }
        gaSolicitorDetailsGAspec.setEmail(idamUserDetails.getEmail());
        gaSolicitorDetailsGAspec.setForename(party.getIndividualFirstName());
        gaSolicitorDetailsGAspec.setSurname(Optional.ofNullable(party.getIndividualLastName()));
        return true;
    }

    private void populateApplicantSolicitorDetails(GASolicitorDetailsGAspec gaSolicitorDetailsGAspec,
                                                   CaseAssignmentUserRole solicitor,
                                                   CaseData caseData) {
        if (caseData.getApplicantSolicitor1UserDetails() == null) {
            return;
        }
        gaSolicitorDetailsGAspec.setId(solicitor.getUserId());
        gaSolicitorDetailsGAspec.setEmail(caseData.getApplicantSolicitor1UserDetails().getEmail());
        gaSolicitorDetailsGAspec.setOrganisationIdentifier(
            caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID()
        );
    }

    private void populateRespondentOneSolicitorDetails(GASolicitorDetailsGAspec gaSolicitorDetailsGAspec,
                                                       CaseAssignmentUserRole solicitor,
                                                       CaseData caseData) {
        gaSolicitorDetailsGAspec.setId(solicitor.getUserId());
        gaSolicitorDetailsGAspec.setEmail(caseData.getRespondentSolicitor1EmailAddress());
        gaSolicitorDetailsGAspec.setOrganisationIdentifier(getRespondent1SolicitorOrgId(caseData));
    }

    private void populateRespondentTwoSolicitorDetails(GASolicitorDetailsGAspec gaSolicitorDetailsGAspec,
                                                       CaseAssignmentUserRole solicitor,
                                                       CaseData caseData) {
        if (!YES.equals(caseData.getAddRespondent2())) {
            return;
        }
        gaSolicitorDetailsGAspec.setId(solicitor.getUserId());
        gaSolicitorDetailsGAspec.setEmail(caseData.getRespondentSolicitor2EmailAddress());
        gaSolicitorDetailsGAspec.setOrganisationIdentifier(getRespondent2SolicitorOrgId(caseData));
    }

    private void addSolicitorIfPresent(List<Element<GASolicitorDetailsGAspec>> addedSols,
                                       GASolicitorDetailsGAspec gaSolicitorDetailsGAspec) {
        if (Objects.nonNull(gaSolicitorDetailsGAspec.getId())) {
            addedSols.add(element(gaSolicitorDetailsGAspec));
        }
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
        return applicantSol
            .map(CaseAssignmentUserRole::getCaseRole)
            .map(caseRole -> resolveApplicantPartyData(
                caseData,
                caseRole,
                applicant1OrgCaseRole,
                applicant2OrgCaseRole,
                respondent1OrgCaseRole,
                respondent2OrgCaseRole
            ))
            .orElseGet(GAParties::new);
    }

    private GAParties resolveApplicantPartyData(CaseData caseData,
                                                String caseRole,
                                                String applicant1OrgCaseRole,
                                                String applicant2OrgCaseRole,
                                                String respondent1OrgCaseRole,
                                                String respondent2OrgCaseRole) {
        GAParties applicantParty = resolveApplicantOneParty(caseData, caseRole, applicant1OrgCaseRole);
        if (applicantParty != null) {
            return applicantParty;
        }

        applicantParty = resolveApplicantTwoParty(caseData, caseRole, applicant2OrgCaseRole);
        if (applicantParty != null) {
            return applicantParty;
        }

        applicantParty = resolveRespondentOneParty(caseData, caseRole, respondent1OrgCaseRole);
        if (applicantParty != null) {
            return applicantParty;
        }

        applicantParty = resolveRespondentTwoParty(caseData, caseRole, respondent2OrgCaseRole);
        return applicantParty != null ? applicantParty : new GAParties();
    }

    private GAParties resolveApplicantOneParty(CaseData caseData, String caseRole, String applicant1OrgCaseRole) {
        /*GA for Lips is only 1v1*/
        if (applicant1OrgCaseRole.equals(caseRole) || CaseRole.CLAIMANT.getFormattedName().equals(caseRole)) {
            return buildApplicantParty(caseData.getApplicant1().getPartyName(), APPLICANT_ID);
        }
        return null;
    }

    private GAParties resolveApplicantTwoParty(CaseData caseData, String caseRole, String applicant2OrgCaseRole) {
        if (applicant2OrgCaseRole.equals(caseRole) && caseData.getApplicant2() != null) {
            return buildApplicantParty(caseData.getApplicant2().getPartyName(), APPLICANT2_ID);
        }
        return null;
    }

    private GAParties resolveRespondentOneParty(CaseData caseData, String caseRole, String respondent1OrgCaseRole) {
        /*GA for Lips is only 1v1*/
        if (respondent1OrgCaseRole.equals(caseRole) || CaseRole.DEFENDANT.getFormattedName().equals(caseRole)) {
            return buildApplicantParty(caseData.getRespondent1().getPartyName(), RESPONDENT_ID);
        }
        return null;
    }

    private GAParties resolveRespondentTwoParty(CaseData caseData, String caseRole, String respondent2OrgCaseRole) {
        if (respondent2OrgCaseRole.equals(caseRole) && caseData.getRespondent2() != null) {
            return buildApplicantParty(caseData.getRespondent2().getPartyName(), RESPONDENT2_ID);
        }
        return null;
    }

    private GAParties buildApplicantParty(String applicantPartyName, String litigiousPartyId) {
        return new GAParties()
            .setApplicantPartyName(applicantPartyName)
            .setLitigiousPartyID(litigiousPartyId);
    }

    public boolean isGAApplicantSameAsParentCaseClaimant(CaseData caseData, String authToken) {
        String parentCaseId = caseData.getCcdCaseReference().toString();
        List<String> userRolesCaching = userRoleCaching.getUserRoles(authToken, parentCaseId);

        boolean isApplicantSolicitor = UserRoleUtils.isApplicantSolicitor(userRolesCaching);

        String applicant1OrgCaseRole = caseData.getApplicant1OrganisationPolicy().getOrgPolicyCaseAssignedRole();

        if (!CollectionUtils.isEmpty(userRolesCaching) && userRolesCaching.size() == 1 && isApplicantSolicitor) {

            String applnSol = userRolesCaching.getFirst();

            return applnSol != null && applnSol.equals(applicant1OrgCaseRole);
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

    private record SolicitorAssignments(List<CaseAssignmentUserRole> applicantAdditionalSolicitors,
                                        List<CaseAssignmentUserRole> respondentSolicitors) {
    }

}
