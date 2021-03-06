package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class InitiateGeneralApplicationService {

    private final InitiateGeneralApplicationServiceHelper helper;
    private final GeneralAppsDeadlinesCalculator deadlinesCalculator;
    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;

    private final OrganisationApi organisationApi;
    private final AuthTokenGenerator authTokenGenerator;

    private static final int NUMBER_OF_DEADLINE_DAYS = 5;
    public static final String URGENCY_DATE_REQUIRED = "Details of urgency consideration date required.";
    public static final String URGENCY_DATE_SHOULD_NOT_BE_PROVIDED = "Urgency consideration date should not be "
        + "provided for a non-urgent application.";
    public static final String URGENCY_DATE_CANNOT_BE_IN_PAST = "The date entered cannot be in the past.";
    public static final String TRIAL_DATE_FROM_REQUIRED = "Please enter the Date from if the trial has been fixed";
    public static final String INVALID_TRIAL_DATE_RANGE = "Trial Date From cannot be after Trial Date to. "
        + "Please enter valid range.";
    public static final String UNAVAILABLE_DATE_RANGE_MISSING = "Please provide at least one valid Date from if you "
        + "cannot attend hearing within next 3 months.";
    public static final String UNAVAILABLE_FROM_MUST_BE_PROVIDED = "If you selected option to be unavailable then "
        + "you must provide at least one valid Date from";
    public static final String INVALID_UNAVAILABILITY_RANGE = "Unavailability Date From cannot be after "
        + "Unavailability Date to. Please enter valid range.";

    public CaseData buildCaseData(CaseData.CaseDataBuilder dataBuilder, CaseData caseData, UserDetails userDetails,
                                  String authToken) {
        List<Element<GeneralApplication>> applications =
            addApplication(buildApplication(caseData, userDetails, authToken), caseData.getGeneralApplications());

        return dataBuilder
            .generalApplications(applications)
            .generalAppType(GAApplicationType.builder().build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().build())
            .generalAppPBADetails(GAPbaDetails.builder().build())
            .generalAppDetailsOfOrder(EMPTY)
            .generalAppReasonsOfOrder(EMPTY)
            .generalAppInformOtherParty(GAInformOtherParty.builder().build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
            .generalAppHearingDetails(GAHearingDetails.builder().build())
            .generalAppEvidenceDocument(java.util.Collections.emptyList())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().build())
            .build();
    }

    private GeneralApplication buildApplication(CaseData caseData, UserDetails userDetails, String authToken) {
        String caseType = "";

        GeneralApplication.GeneralApplicationBuilder applicationBuilder = GeneralApplication.builder();
        if (caseData.getGeneralAppEvidenceDocument() != null) {
            applicationBuilder.generalAppEvidenceDocument(caseData.getGeneralAppEvidenceDocument());
        }
        if (MultiPartyScenario.isMultiPartyScenario(caseData)) {
            applicationBuilder.isMultiParty(YES);
        } else {
            applicationBuilder.isMultiParty(NO);
        }
        applicationBuilder.claimant1PartyName(caseData.getApplicant1().getPartyName());
        applicationBuilder.defendant1PartyName(caseData.getRespondent1().getPartyName());
        if (YES.equals(caseData.getAddApplicant2())) {
            applicationBuilder.claimant2PartyName(caseData.getApplicant2().getPartyName());
        }
        if (YES.equals(caseData.getAddRespondent2())) {
            applicationBuilder.defendant2PartyName(caseData.getRespondent2().getPartyName());
        }
        if (caseData.getSuperClaimType() != null && caseData.getSuperClaimType().equals(SPEC_CLAIM)) {
            caseType = "SPEC_CLAIM";
        } else {
            caseType = "UNSPEC_CLAIM";
        }
        LocalDateTime deadline = deadlinesCalculator
            .calculateApplicantResponseDeadline(
                LocalDateTime.now(), NUMBER_OF_DEADLINE_DAYS);
        if (caseData.getGeneralAppRespondentAgreement() != null
            && NO.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed())) {
            applicationBuilder
                .generalAppInformOtherParty(caseData.getGeneralAppInformOtherParty())
                .generalAppStatementOfTruth(caseData.getGeneralAppStatementOfTruth());
        } else {
            applicationBuilder
                .generalAppInformOtherParty(GAInformOtherParty.builder().build())
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().build());
        }

        Optional<Organisation> org = findOrganisation(authToken);
        if (org.isPresent()) {
            applicationBuilder
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                                             .builder()
                                             .id(userDetails.getId())
                                             .email(userDetails.getEmail())
                                             .forename(userDetails.getForename())
                                             .surname(userDetails.getSurname())
                                             .organisationIdentifier(org.get().getOrganisationIdentifier()).build());
        }

        GeneralApplication generalApplication = applicationBuilder
            .businessProcess(BusinessProcess.ready(INITIATE_GENERAL_APPLICATION))
            .generalAppType(caseData.getGeneralAppType())
            .generalAppRespondentAgreement(caseData.getGeneralAppRespondentAgreement())
            .generalAppUrgencyRequirement(caseData.getGeneralAppUrgencyRequirement())
            .generalAppDetailsOfOrder(caseData.getGeneralAppDetailsOfOrder())
            .generalAppReasonsOfOrder(caseData.getGeneralAppReasonsOfOrder())
            .generalAppHearingDetails(caseData.getGeneralAppHearingDetails())
            .generalAppPBADetails(caseData.getGeneralAppPBADetails())
            .generalAppDateDeadline(deadline)
            .generalAppSubmittedDateGAspec(LocalDateTime.now())
            .generalAppSuperClaimType(caseType)
            .civilServiceUserRoles(IdamUserDetails.builder().id(userDetails.getId()).email(userDetails.getEmail())
                                       .build())
            .build();

        return helper.setRespondentDetailsIfPresent(generalApplication, caseData, userDetails);
    }

    private List<Element<GeneralApplication>> addApplication(GeneralApplication application,
                                                             List<Element<GeneralApplication>>
                                                                 generalApplicationDetails) {
        List<Element<GeneralApplication>> newApplication = ofNullable(generalApplicationDetails).orElse(newArrayList());
        newApplication.add(element(application));

        return newApplication;
    }

    public List<String> validateUrgencyDates(GAUrgencyRequirement generalAppUrgencyRequirement) {
        List<String> errors = new ArrayList<>();
        if (generalAppUrgencyRequirement.getGeneralAppUrgency() == YES
            && generalAppUrgencyRequirement.getUrgentAppConsiderationDate() == null) {
            errors.add(URGENCY_DATE_REQUIRED);
        }
        if (generalAppUrgencyRequirement.getGeneralAppUrgency() == NO
            && generalAppUrgencyRequirement.getUrgentAppConsiderationDate() != null) {
            errors.add(URGENCY_DATE_SHOULD_NOT_BE_PROVIDED);
        }

        if (generalAppUrgencyRequirement.getGeneralAppUrgency() == YES
            && generalAppUrgencyRequirement.getUrgentAppConsiderationDate() != null) {
            LocalDate urgencyDate = generalAppUrgencyRequirement.getUrgentAppConsiderationDate();
            if (LocalDate.now().isAfter(urgencyDate)) {
                errors.add(URGENCY_DATE_CANNOT_BE_IN_PAST);
            }
        }
        return errors;
    }

    public List<String> validateHearingScreen(GAHearingDetails hearingDetails) {
        List<String> errors = new ArrayList<>();
        validateTrialDate(errors, hearingDetails.getTrialRequiredYesOrNo(), hearingDetails.getTrialDateFrom(),
                          hearingDetails.getTrialDateTo()
        );
        validateUnavailableDates(errors, hearingDetails.getUnavailableTrialRequiredYesOrNo(),
                                 hearingDetails.getGeneralAppUnavailableDates()
        );
        return errors;
    }

    private void validateTrialDate(List<String> errors,
                                   YesOrNo isTrialScheduled,
                                   LocalDate trialDateFrom,
                                   LocalDate trialDateTo) {
        if (YES.equals(isTrialScheduled)) {
            if (trialDateFrom == null) {
                errors.add(TRIAL_DATE_FROM_REQUIRED);
            } else if (trialDateTo != null && trialDateFrom.compareTo(trialDateTo) > 0) {
                errors.add(INVALID_TRIAL_DATE_RANGE);
            }
        }
    }

    private void validateUnavailableDates(List<String> errors,
                                          YesOrNo isUnavailable,
                                          List<Element<GAUnavailabilityDates>> datesUnavailableList) {
        if (YES.equals(isUnavailable)) {
            if (isEmpty(datesUnavailableList)) {
                errors.add(UNAVAILABLE_DATE_RANGE_MISSING);
            } else {
                datesUnavailableList.forEach(dateRange -> {
                    LocalDate dateFrom = dateRange.getValue().getUnavailableTrialDateFrom();
                    LocalDate dateTo = dateRange.getValue().getUnavailableTrialDateTo();
                    if (dateFrom == null) {
                        errors.add(UNAVAILABLE_FROM_MUST_BE_PROVIDED);
                    } else if (dateTo != null && dateFrom.compareTo(dateTo) > 0) {
                        errors.add(INVALID_UNAVAILABILITY_RANGE);
                    }
                });
            }
        }
    }

    public Optional<Organisation> findOrganisation(String authToken) {
        try {
            return ofNullable(organisationApi.findUserOrganisation(authToken, authTokenGenerator.generate()));

        } catch (FeignException.NotFound | FeignException.Forbidden ex) {
            log.error("User not registered in MO", ex);
            return Optional.empty();
        }
    }

    public boolean respondentAssigned(CaseData caseData) {
        String caseId = caseData.getCcdCaseReference().toString();
        CaseAssignedUserRolesResource userRoles = getUserRolesOnCase(caseId);
        List<String> respondentCaseRoles = getRespondentCaseRoles(caseData);

        for (String respondentCaseRole : respondentCaseRoles) {
            if (userRoles.getCaseAssignedUserRoles() == null
                    || userRoles.getCaseAssignedUserRoles().stream()
                    .noneMatch(a -> a.getCaseRole() != null && respondentCaseRole.equals(a.getCaseRole()))) {
                return false;
            }
        }
        return true;
    }

    private CaseAssignedUserRolesResource getUserRolesOnCase(String caseId) {
        String accessToken = userService.getAccessToken(
                crossAccessUserConfiguration.getUserName(),
                crossAccessUserConfiguration.getPassword()
        );
        return caseAccessDataStoreApi.getUserRoles(
                accessToken,
                authTokenGenerator.generate(),
                List.of(caseId)
        );
    }

    private List<String> getRespondentCaseRoles(CaseData caseData) {
        List<String> respondentCaseRoles = new ArrayList<>();
        respondentCaseRoles.add(caseData.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole());
        if (NO.equals(caseData.getRespondent2SameLegalRepresentative())
                && caseData.getRespondent2OrganisationPolicy() != null) {
            respondentCaseRoles.add(caseData.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }
        return respondentCaseRoles;
    }
}
