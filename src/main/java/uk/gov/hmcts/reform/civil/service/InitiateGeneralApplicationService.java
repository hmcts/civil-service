package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.model.Party.Type.SOLE_TRADER;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class InitiateGeneralApplicationService {

    private final InitiateGeneralApplicationServiceHelper helper;
    private final GeneralAppsDeadlinesCalculator deadlinesCalculator;
    private final UserRoleCaching userRoleCaching;
    private final LocationRefDataService locationRefDataService;

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

    private static final List<CaseState> statesBeforeSDO = Arrays.asList(PENDING_CASE_ISSUED, CASE_ISSUED,
            AWAITING_CASE_DETAILS_NOTIFICATION, AWAITING_RESPONDENT_ACKNOWLEDGEMENT, CASE_DISMISSED,
            AWAITING_APPLICANT_INTENTION, PROCEEDS_IN_HERITAGE_SYSTEM);

    public CaseData buildCaseData(CaseData.CaseDataBuilder dataBuilder, CaseData caseData, UserDetails userDetails,
                                  String authToken) {
        List<Element<GeneralApplication>> applications =
            addApplication(buildApplication(dataBuilder, caseData, userDetails, authToken), caseData.getGeneralApplications());

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
            .build();
    }

    private GeneralApplication buildApplication(CaseData.CaseDataBuilder dataBuilder,
                                                CaseData caseData, UserDetails userDetails, String authToken) {
        CaseCategory caseType;

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
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            caseType = CaseCategory.SPEC_CLAIM;
        } else {
            caseType = CaseCategory.UNSPEC_CLAIM;
        }

        if (caseData.getGeneralAppRespondentAgreement() != null) {
            if (YES.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed())
                    && !caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_JUDGEMENT)) {

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

        Pair<CaseLocationCivil, Boolean> caseLocation = getWorkAllocationLocation(caseData, authToken);
        //Setting Work Allocation location and location name
        applicationBuilder.caseManagementLocation(caseLocation.getLeft());
        applicationBuilder.isCtscLocation(caseLocation.getRight() ? YES : NO);
        applicationBuilder.locationName(hasSDOBeenMade(caseData.getCcdState())
                                            ? caseData.getLocationName() : caseLocation.getLeft().getSiteName());

        LocalDateTime deadline = deadlinesCalculator
            .calculateApplicantResponseDeadline(
                LocalDateTime.now(), NUMBER_OF_DEADLINE_DAYS);

        if (caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_JUDGEMENT)
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

        GeneralApplication generalApplication = applicationBuilder
            .businessProcess(BusinessProcess.ready(INITIATE_GENERAL_APPLICATION))
            .generalAppType(caseData.getGeneralAppType())
            .generalAppHearingDate(caseData.getGeneralAppHearingDate())
            .generalAppRespondentAgreement(caseData.getGeneralAppRespondentAgreement())
            .generalAppUrgencyRequirement(caseData.getGeneralAppUrgencyRequirement())
            .generalAppDetailsOfOrder(caseData.getGeneralAppDetailsOfOrder())
            .generalAppReasonsOfOrder(caseData.getGeneralAppReasonsOfOrder())
            .generalAppHearingDetails(caseData.getGeneralAppHearingDetails())
            .generalAppPBADetails(caseData.getGeneralAppPBADetails())
            .generalAppDateDeadline(deadline)
            .generalAppSubmittedDateGAspec(LocalDateTime.now())
            .generalAppSuperClaimType(caseType.name())
            .caseAccessCategory(caseType)
            .civilServiceUserRoles(IdamUserDetails.builder().id(userDetails.getId()).email(userDetails.getEmail())
                                       .build())
            .build();

        return helper.setRespondentDetailsIfPresent(dataBuilder, generalApplication, caseData, userDetails);
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

    public boolean respondentAssigned(CaseData caseData, String authToken) {
        String caseId = caseData.getCcdCaseReference().toString();
        List<String> userRoles = userRoleCaching.getUserRoles(authToken, caseId);
        List<String> respondentCaseRoles = getRespondentCaseRoles(caseData);
        return !(userRoles.isEmpty() || !isRespondentSolicitorOne(respondentCaseRoles)
            || (respondentCaseRoles.size() > 1 && !isRespondentSolicitorTwo(respondentCaseRoles)));
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
            if (!(MultiPartyScenario.isMultiPartyScenario(caseData))) {
                if (INDIVIDUAL.equals(caseData.getRespondent1().getType())
                    || SOLE_TRADER.equals(caseData.getRespondent1().getType())) {
                    return Pair.of(getDefendant1PreferredLocation(caseData), false);
                } else {
                    return Pair.of(getClaimant1PreferredLocation(caseData), false);
                }
            } else {
                if (INDIVIDUAL.equals(caseData.getRespondent1().getType())
                    || SOLE_TRADER.equals(caseData.getRespondent1().getType())
                    || INDIVIDUAL.equals(caseData.getRespondent2().getType())
                    || SOLE_TRADER.equals(caseData.getRespondent2().getType())) {

                    return Pair.of(getDefendantPreferredLocation(caseData), false);
                } else {
                    return Pair.of(getClaimant1PreferredLocation(caseData), false);
                }
            }
        } else {
            LocationRefData ctscLocation;
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                ctscLocation = locationRefDataService.getCtscLocation(authToken, true);
            } else {
                ctscLocation = locationRefDataService.getCtscLocation(authToken, false);
            }

            CaseLocationCivil courtLocation = CaseLocationCivil.builder()
                .region(ctscLocation.getRegionId())
                .baseLocation(ctscLocation.getEpimmsId())
                .siteName(ctscLocation.getSiteName())
                .build();
            return Pair.of(courtLocation, true);

        }
    }

    private boolean hasSDOBeenMade(CaseState state) {
        return !statesBeforeSDO.contains(state);
    }

    private CaseLocationCivil getClaimant1PreferredLocation(CaseData caseData) {
        if (caseData.getApplicant1DQ() == null
                || caseData.getApplicant1DQ().getApplicant1DQRequestedCourt() == null
                || caseData.getApplicant1DQ().getApplicant1DQRequestedCourt().getResponseCourtCode() == null) {
            return CaseLocationCivil.builder()
                .region(caseData.getCourtLocation().getCaseLocation().getRegion())
                .baseLocation(caseData.getCourtLocation().getCaseLocation().getBaseLocation())
                .build();
        }
        return CaseLocationCivil.builder()
            .region(caseData.getApplicant1DQ().getApplicant1DQRequestedCourt()
                        .getCaseLocation().getRegion())
            .baseLocation(caseData.getApplicant1DQ().getApplicant1DQRequestedCourt()
                              .getCaseLocation().getBaseLocation())
            .build();
    }

    private boolean isDefendant1RespondedFirst(CaseData caseData) {
        return caseData.getRespondent2ResponseDate() == null
                || (caseData.getRespondent1ResponseDate() != null
                && !caseData.getRespondent1ResponseDate().isAfter(caseData.getRespondent2ResponseDate()));
    }

    private CaseLocationCivil getDefendant1PreferredLocation(CaseData caseData) {
        if (caseData.getRespondent1DQ() == null
                || caseData.getRespondent1DQ().getRespondent1DQRequestedCourt() == null
                || caseData.getRespondent1DQ().getRespondent1DQRequestedCourt().getResponseCourtCode() == null) {
            return CaseLocationCivil.builder().build();
        }
        return CaseLocationCivil.builder()
            .region(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt()
                        .getCaseLocation().getRegion())
            .baseLocation(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt()
                              .getCaseLocation().getBaseLocation())
            .build();
    }

    private CaseLocationCivil getDefendantPreferredLocation(CaseData caseData) {
        if (isDefendant1RespondedFirst(caseData) & !(caseData.getRespondent1DQ() == null
            || caseData.getRespondent1DQ().getRespondent1DQRequestedCourt() == null)) {

            return CaseLocationCivil.builder()
                .region(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt()
                            .getCaseLocation().getRegion())
                .baseLocation(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt()
                                  .getCaseLocation().getBaseLocation())
                .build();
        } else if (!(isDefendant1RespondedFirst(caseData)) || !(caseData.getRespondent2DQ() == null
            || caseData.getRespondent2DQ().getRespondent2DQRequestedCourt() == null)) {
            return CaseLocationCivil.builder()
                .region(caseData.getRespondent2DQ().getRespondent2DQRequestedCourt()
                            .getCaseLocation().getRegion())
                .baseLocation(caseData.getRespondent2DQ().getRespondent2DQRequestedCourt()
                                  .getCaseLocation().getBaseLocation())
                .build();
        } else {
            return CaseLocationCivil.builder().build();
        }
    }
}
