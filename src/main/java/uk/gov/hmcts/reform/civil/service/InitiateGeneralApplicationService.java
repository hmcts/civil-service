package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.DebtPaymentOptions;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseManagementCategoryElement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRANSFER_ONLINE_CASE;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class InitiateGeneralApplicationService {

    private final InitiateGeneralApplicationServiceHelper helper;
    private final GeneralAppsDeadlinesCalculator deadlinesCalculator;
    private final LocationReferenceDataService locationRefDataService;
    private final CaseAssignmentApi caseAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final FeatureToggleService featureToggleService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;
    private final CoreCaseEventDataService coreCaseEventDataService;
    private final Time time;

    private static final int NUMBER_OF_DEADLINE_DAYS = 5;
    public static final String GA_DOC_CATEGORY_ID = "applications";
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
    public static final String INVALID_SETTLE_BY_CONSENT = "Settle by consent " +
            "must have been agreed with the respondent " +
            "before raising the application";
    public static final List<String> lipCaseRole = Arrays.asList("[DEFENDANT]", "[CLAIMANT]");

    private static final List<CaseState> statesBeforeSDO = Arrays.asList(PENDING_CASE_ISSUED, CASE_ISSUED,
            AWAITING_CASE_DETAILS_NOTIFICATION, AWAITING_RESPONDENT_ACKNOWLEDGEMENT, IN_MEDIATION,
            AWAITING_APPLICANT_INTENTION);
    public static final String MULTI_CLAIM_TRACK = " - Multi Track";
    public static final String INTERMEDIATE_CLAIM_TRACK = " - Intermediate Track";
    public static final String SMALL_CLAIM_TRACK = " - Small Claims";
    public static final String FAST_CLAIM_TRACK = " - Fast Track";

    public CaseData buildCaseData(CaseData.CaseDataBuilder dataBuilder, CaseData caseData, UserDetails userDetails,
                                  String authToken, GeneralAppFeesService feesService) {
        List<Element<GeneralApplication>> applications =
            addApplication(buildApplication(caseData, userDetails, authToken, feesService), caseData.getGeneralApplications());

        return dataBuilder
            .generalApplications(applications)
            .generalAppType(GAApplicationType.builder().build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().build())
            .generalAppPBADetails(GAPbaDetails.builder().build())
            .generalAppDetailsOfOrder(EMPTY)
            .generalAppReasonsOfOrder(EMPTY)
            .generalAppParentClaimantIsApplicant(null)
            .generalAppVaryJudgementType(null)
            .generalAppN245FormUpload(Document.builder().build())
            .generalAppHearingDate(GAHearingDateGAspec.builder().build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
            .generalAppHearingDetails(GAHearingDetails.builder().build())
            .generalAppEvidenceDocument(java.util.Collections.emptyList())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().build())
            .gaWaTrackLabel(null)
            .build();
    }

    private GeneralApplication buildApplication(CaseData caseData, UserDetails userDetails, String authToken, GeneralAppFeesService feesService) {

        GeneralApplication.GeneralApplicationBuilder applicationBuilder = GeneralApplication.builder();
        if (caseData.getGeneralAppEvidenceDocument() != null) {
            applicationBuilder.generalAppEvidenceDocument(caseData.getGeneralAppEvidenceDocument());
        }
        if (MultiPartyScenario.isMultiPartyScenario(caseData)) {
            applicationBuilder.isMultiParty(YES);
        } else {
            applicationBuilder.isMultiParty(NO);
        }
        applicationBuilder.claimant1PartyName(getPartyNameBasedOnType(caseData.getApplicant1()));
        applicationBuilder.defendant1PartyName(getPartyNameBasedOnType(caseData.getRespondent1()));
        if (YES.equals(caseData.getAddApplicant2())) {
            applicationBuilder.claimant2PartyName(getPartyNameBasedOnType(caseData.getApplicant2()));
        }
        if (YES.equals(caseData.getAddRespondent2())) {
            applicationBuilder.defendant2PartyName(getPartyNameBasedOnType(caseData.getRespondent2()));
        }
        applicationBuilder.emailPartyReference(buildPartiesReferencesEmailSubject(caseData));
        final var caseType = SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            ? CaseCategory.SPEC_CLAIM
            : CaseCategory.UNSPEC_CLAIM;

        if (caseData.getGeneralAppRespondentAgreement() != null) {
            if (YES.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed())
                    && !caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)) {

                GAStatementOfTruth gaStatementOfTruth = ofNullable(caseData.getGeneralAppStatementOfTruth())
                    .map(GAStatementOfTruth::toBuilder)
                    .orElse(GAStatementOfTruth.builder())
                    .build();
                applicationBuilder
                        .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                        .generalAppConsentOrder(NO)
                        .generalAppStatementOfTruth(gaStatementOfTruth);
            } else {
                applicationBuilder
                        .generalAppInformOtherParty(caseData.getGeneralAppInformOtherParty())
                        .generalAppStatementOfTruth(caseData.getGeneralAppStatementOfTruth());
            }
        } else {
            applicationBuilder
                .generalAppInformOtherParty(GAInformOtherParty.builder().build())
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().build());
        }

        GACaseManagementCategoryElement civil =
            GACaseManagementCategoryElement.builder().code("Civil").label("Civil").build();
        List<Element<GACaseManagementCategoryElement>> itemList = new ArrayList<>();
        itemList.add(element(civil));
        applicationBuilder.caseManagementCategory(
            GACaseManagementCategory.builder().value(civil).list_items(itemList).build());
        if (featureToggleService.isGaForLipsEnabled() && caseContainsLiP(caseData) && hasSDOBeenMade(caseData.getCcdState())) {
            LocationRefData  locationDetails = getWorkAllocationLocationDetails(caseData.getCaseManagementLocation().getBaseLocation(), authToken);
            applicationBuilder.caseManagementLocation(CaseLocationCivil.builder()
                                                          .region(caseData.getCaseManagementLocation().getRegion())
                                                          .baseLocation(caseData.getCaseManagementLocation().getBaseLocation())
                                                          .siteName(locationDetails.getSiteName())
                                                          .address(locationDetails.getCourtAddress())
                                                          .postcode(locationDetails.getPostcode())
                                                          .build());
            applicationBuilder.locationName(locationDetails.getSiteName());
            applicationBuilder.isCcmccLocation(NO);
        } else {
            Pair<CaseLocationCivil, Boolean> caseLocation = getWorkAllocationLocation(caseData, authToken);
            if (Objects.isNull(caseLocation.getLeft().getBaseLocation()) && !caseLocation.getRight()) {
                caseLocation.getLeft().setBaseLocation(caseData.getCaseManagementLocation().getBaseLocation());
                caseLocation.getLeft().setRegion(caseData.getCaseManagementLocation().getRegion());
            }
            //Setting Work Allocation location and location name
            if (Objects.isNull(caseLocation.getLeft().getSiteName())
                && Objects.nonNull(caseLocation.getLeft().getBaseLocation())) {
                LocationRefData  locationDetails = getWorkAllocationLocationDetails(caseLocation.getLeft().getBaseLocation(), authToken);
                caseLocation.getLeft().setSiteName(locationDetails.getSiteName());
                caseLocation.getLeft().setAddress(locationDetails.getCourtAddress());
                caseLocation.getLeft().setPostcode(locationDetails.getPostcode());
            }
            applicationBuilder.caseManagementLocation(caseLocation.getLeft());
            applicationBuilder.locationName(hasSDOBeenMade(caseData.getCcdState())
                                                ? caseData.getLocationName() : caseLocation.getLeft().getSiteName());
            applicationBuilder.isCcmccLocation(caseLocation.getRight() ? YES : NO);
        }
        LocalDateTime deadline = deadlinesCalculator
            .calculateApplicantResponseDeadline(
                LocalDateTime.now(), NUMBER_OF_DEADLINE_DAYS);

        if (caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)
            && ! Objects.isNull(caseData.getGeneralAppN245FormUpload())) {
            if (Objects.isNull(caseData.getGeneralAppN245FormUpload().getCategoryID())) {
                caseData.getGeneralAppN245FormUpload().setCategoryID(GA_DOC_CATEGORY_ID);
            }
            applicationBuilder.generalAppN245FormUpload(caseData.getGeneralAppN245FormUpload());
            List<Element<Document>> gaEvidenceDoc = ofNullable(caseData.getGeneralAppEvidenceDocument())
                    .orElse(newArrayList());
            gaEvidenceDoc.add(element(caseData.getGeneralAppN245FormUpload()));
            applicationBuilder.generalAppEvidenceDocument(gaEvidenceDoc);
        }

        applicationBuilder
            .businessProcess(BusinessProcess.ready(INITIATE_GENERAL_APPLICATION))
            .generalAppType(caseData.getGeneralAppType())
            .generalAppHearingDate(caseData.getGeneralAppHearingDate())
            .generalAppRespondentAgreement(caseData.getGeneralAppRespondentAgreement())
            .generalAppUrgencyRequirement(caseData.getGeneralAppUrgencyRequirement())
            .generalAppDetailsOfOrder(caseData.getGeneralAppDetailsOfOrder())
            .generalAppDetailsOfOrderColl(caseData.getGeneralAppDetailsOfOrderColl())
            .generalAppReasonsOfOrder(caseData.getGeneralAppReasonsOfOrder())
            .generalAppReasonsOfOrderColl(caseData.getGeneralAppReasonsOfOrderColl())
            .generalAppHearingDetails(caseData.getGeneralAppHearingDetails())
            .generalAppHelpWithFees(caseData.getGeneralAppHelpWithFees())
            .generalAppPBADetails(caseData.getGeneralAppPBADetails())
            .generalAppAskForCosts(caseData.getGeneralAppAskForCosts())
            .generalAppDateDeadline(deadline)
            .generalAppSubmittedDateGAspec(LocalDateTime.now())
            .generalAppSuperClaimType(caseType.name())
            .caseAccessCategory(caseType)
            .civilServiceUserRoles(IdamUserDetails.builder().id(userDetails.getId()).email(userDetails.getEmail())
                                       .build());

        if (featureToggleService.isGaForLipsEnabled()) {
            applicationBuilder.isGaApplicantLip(NO)
                .isGaRespondentOneLip(NO)
                .isGaRespondentTwoLip(NO);
            if (caseData.isRespondent1LiP() || caseData.isRespondent2LiP() || caseData.isApplicantNotRepresented()) {
                applicationBuilder.generalAppSubmittedDateGAspec(time.now());
            }
        }
        if (featureToggleService.isCoSCEnabled()
            && caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID)) {
            if (Objects.nonNull(caseData.getCertOfSC().getDebtPaymentEvidence())
                && Objects.nonNull(caseData.getCertOfSC().getDebtPaymentEvidence().getDebtPaymentOption())) {
                DebtPaymentOptions deptPaymentOption = caseData.getCertOfSC().getDebtPaymentEvidence().getDebtPaymentOption();
                if (DebtPaymentOptions.MADE_FULL_PAYMENT_TO_COURT.equals(deptPaymentOption)
                    || DebtPaymentOptions.UPLOAD_EVIDENCE_DEBT_PAID_IN_FULL.equals(deptPaymentOption)) {
                    caseData.getCertOfSC().setProofOfDebtDoc(caseData.getGeneralAppEvidenceDocument());
                } else {
                    caseData.getCertOfSC().setProofOfDebtDoc(java.util.Collections.emptyList());
                }
            }
            applicationBuilder.certOfSC(caseData.getCertOfSC());
        }
        applicationBuilder.caseNameGaInternal(caseData.getCaseNameHmctsInternal());
        if (featureToggleService.isMintiEnabled()) {
            applicationBuilder.gaWaTrackLabel(setClaimTrackForTaskName(caseData));
        }
        return helper.setRespondentDetailsIfPresent(applicationBuilder.build(), caseData, userDetails, feesService);
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

    public boolean respondentAssigned(CaseData caseData) {
        String caseId = caseData.getCcdCaseReference().toString();
        CaseAssignmentUserRolesResource userRoles = getUserRolesOnCase(caseId);
        List<String> respondentCaseRoles = getRespondentCaseRoles(caseData);

        if (featureToggleService.isGaForLipsEnabled() && (caseData.isRespondent1LiP() || caseData.isRespondent2LiP()
            || caseData.isApplicantNotRepresented())) {

            for (String lipRole : lipCaseRole) {
                if (userRoles.getCaseAssignmentUserRoles() != null && userRoles.getCaseAssignmentUserRoles().size() > 1
                    || userRoles.getCaseAssignmentUserRoles().stream()
                    .anyMatch(role -> role.getCaseRole().equals(lipRole))) {
                    return true;
                }
            }
            return false;
        }

        for (String respondentCaseRole : respondentCaseRoles) {
            if (userRoles.getCaseAssignmentUserRoles() == null
                || userRoles.getCaseAssignmentUserRoles().stream()
                .noneMatch(a -> a.getCaseRole() != null && respondentCaseRole.equals(a.getCaseRole()))) {
                return false;
            }
        }
        return true;
    }

    private CaseAssignmentUserRolesResource getUserRolesOnCase(String caseId) {
        String accessToken = userService.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );
        return caseAssignmentApi.getUserRoles(
            accessToken,
            authTokenGenerator.generate(),
            List.of(caseId)
        );
    }

    public boolean caseContainsLiP(CaseData caseData) {
        return caseData.isRespondent1LiP() || caseData.isRespondent2LiP() || caseData.isApplicantNotRepresented();
    }

    public boolean isGAApplicantSameAsParentCaseClaimant(CaseData caseData, String authToken) {
        return helper.isGAApplicantSameAsParentCaseClaimant(caseData, authToken);
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

    public Pair<CaseLocationCivil, Boolean> getWorkAllocationLocation(CaseData caseData, String authToken) {
        if (hasSDOBeenMade(caseData.getCcdState())) {
            return Pair.of(assignCaseManagementLocationToMainCaseLocation(caseData, authToken), false);
        } else {
            return getWorkAllocationLocationBeforeSdo(caseData, authToken);
        }
    }

    private Pair<CaseLocationCivil, Boolean> getWorkAllocationLocationBeforeSdo(CaseData caseData, String authToken) {
        List<CaseEventDetail> caseEventDetails = coreCaseEventDataService.getEventsForCase(caseData.getCcdCaseReference().toString());
        List<String> currentEvents = caseEventDetails.stream().map(CaseEventDetail::getId).toList();
        CaseLocationCivil courtLocation;
        if (currentEvents.contains(TRANSFER_ONLINE_CASE.name())) {
            courtLocation = assignCaseManagementLocationToMainCaseLocation(caseData, authToken);
            return Pair.of(courtLocation, true);
        } else {
            LocationRefData cnbcLocation = locationRefDataService.getCnbcLocation(authToken);
            courtLocation = CaseLocationCivil.builder()
                .region(cnbcLocation.getRegionId())
                .baseLocation(cnbcLocation.getEpimmsId())
                .siteName(cnbcLocation.getSiteName())
                .address(cnbcLocation.getCourtAddress())
                .postcode(cnbcLocation.getPostcode())
                .build();
        }
        return Pair.of(courtLocation, true);
    }

    private boolean hasSDOBeenMade(CaseState state) {
        return !statesBeforeSDO.contains(state);
    }

    public LocationRefData getWorkAllocationLocationDetails(String baseLocation, String authToken) {
        List<LocationRefData> locationDetails = locationRefDataService.getCourtLocationsByEpimmsId(authToken, baseLocation);
        if (locationDetails != null && !locationDetails.isEmpty()) {
            return locationDetails.get(0);
        } else {
            return LocationRefData.builder().build();
        }
    }

    private CaseLocationCivil assignCaseManagementLocationToMainCaseLocation(CaseData caseData, String authToken) {
        LocationRefData caseManagementLocationDetails;
        List<LocationRefData>  locationRefDataList = locationRefDataService.getHearingCourtLocations(authToken);
        var foundLocations = locationRefDataList.stream()
            .filter(location -> location.getEpimmsId().equals(caseData.getCaseManagementLocation().getBaseLocation())).toList();
        if (!foundLocations.isEmpty()) {
            caseManagementLocationDetails = foundLocations.get(0);
        } else {
            throw new IllegalArgumentException("Base Court Location for General applications not found, in location data");
        }
        CaseLocationCivil courtLocation;
        courtLocation = CaseLocationCivil.builder()
            .region(caseManagementLocationDetails.getRegionId())
            .baseLocation(caseManagementLocationDetails.getEpimmsId())
            .siteName(caseManagementLocationDetails.getSiteName())
            .address(caseManagementLocationDetails.getCourtAddress())
            .postcode(caseManagementLocationDetails.getPostcode())
            .build();
        return courtLocation;
    }

    private String setClaimTrackForTaskName(CaseData caseData) {
        String taskTrackName = "";
        if (caseData.getCaseAccessCategory().equals(UNSPEC_CLAIM) && nonNull(caseData.getAllocatedTrack())) {
            taskTrackName =  caseData.getAllocatedTrack().name();
        } else if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM) && nonNull(caseData.getResponseClaimTrack())) {
            taskTrackName =  caseData.getResponseClaimTrack();
        }
        return switch (taskTrackName) {
            case "MULTI_CLAIM" -> MULTI_CLAIM_TRACK;
            case "INTERMEDIATE_CLAIM" -> INTERMEDIATE_CLAIM_TRACK;
            case "SMALL_CLAIM" -> SMALL_CLAIM_TRACK;
            case "FAST_CLAIM" -> FAST_CLAIM_TRACK;
            default -> (" ");
        };
    }
}
