package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
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
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceConstants.FAST_CLAIM_TRACK;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceConstants.GA_DOC_CATEGORY_ID;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceConstants.INTERMEDIATE_CLAIM_TRACK;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceConstants.MULTI_CLAIM_TRACK;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceConstants.SMALL_CLAIM_TRACK;
import static uk.gov.hmcts.reform.civil.service.LocationService.settleDiscontinueStates;
import static uk.gov.hmcts.reform.civil.service.LocationService.statesBeforeSDO;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitiateGeneralApplicationService {

    public static final int GA_CLAIM_DEADLINE_EXTENSION_MONTHS = 36;
    private final InitiateGeneralApplicationServiceHelper helper;
    private final GeneralAppsDeadlinesCalculator deadlinesCalculator;
    private final FeatureToggleService featureToggleService;
    private final CaseAssignmentApi caseAssignmentApi;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;
    private final UserService userService;
    private final AuthTokenGenerator authTokenGenerator;
    private final LocationService locationService;
    private final GeneralAppFeesService feesService;
    private final Time time;
    private final List<String> lipCaseRole = Arrays.asList("[DEFENDANT]", "[CLAIMANT]");

    public CaseData buildCaseData(CaseData caseData, UserDetails userDetails,
                                  String authToken) {
        GeneralApplication generalApplication = buildApplication(caseData, userDetails, authToken);
        List<Element<GeneralApplication>> applications = addApplication(generalApplication, caseData.getGeneralApplications());
        return populateGeneralApplicationData(caseData, applications);
    }

    private CaseData populateGeneralApplicationData(CaseData caseData, List<Element<GeneralApplication>> applications) {
        caseData.setClaimDismissedDeadline(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(
            GA_CLAIM_DEADLINE_EXTENSION_MONTHS,
            LocalDate.now()
        ));
        caseData.setGeneralApplications(applications);
        caseData.setGeneralAppType(new GAApplicationType());
        caseData.setGeneralAppRespondentAgreement(new GARespondentOrderAgreement());
        caseData.setGeneralAppPBADetails(new GAPbaDetails());
        caseData.setGeneralAppDetailsOfOrder(EMPTY);
        caseData.setGeneralAppReasonsOfOrder(EMPTY);
        caseData.setGeneralAppParentClaimantIsApplicant(null);
        caseData.setGeneralAppVaryJudgementType(null);
        caseData.setGeneralAppN245FormUpload(new Document());
        caseData.setGeneralAppHearingDate(new GAHearingDateGAspec());
        caseData.setGeneralAppInformOtherParty(new GAInformOtherParty());
        caseData.setGeneralAppUrgencyRequirement(new GAUrgencyRequirement());
        caseData.setGeneralAppStatementOfTruth(new GAStatementOfTruth());
        caseData.setGeneralAppHearingDetails(new GAHearingDetails());
        caseData.setGeneralAppEvidenceDocument(java.util.Collections.emptyList());
        caseData.setGeneralAppApplnSolicitor(new GASolicitorDetailsGAspec());
        caseData.setGaWaTrackLabel(null);
        return caseData;
    }

    private GeneralApplication buildApplication(CaseData caseData, UserDetails userDetails, String authToken) {
        GeneralApplication application = new GeneralApplication();
        setGeneralAppEvidenceDocument(caseData, application);
        setMultiPartyScenario(caseData, application);
        setPartyNames(caseData, application);
        setCaseType(caseData, application);
        setRespondentAgreement(caseData, application);
        setCaseManagementCategory(application);
        setCaseManagementLocation(caseData, authToken, application);
        setGeneralAppN245FormUpload(caseData, application);
        setBusinessProcess(caseData, userDetails, application);
        setCaseNameGaInternal(caseData, application);
        setFeatureToggles(caseData, application);
        setDates(caseData, application);
        return finalizeApplication(application, caseData, userDetails);
    }

    private GeneralApplication finalizeApplication(GeneralApplication application, CaseData caseData, UserDetails userDetails) {
        return helper.setRespondentDetailsIfPresent(application, caseData, userDetails, feesService);
    }

    private static void setCaseNameGaInternal(CaseData caseData, GeneralApplication application) {
        application.setCaseNameGaInternal(caseData.getCaseNameHmctsInternal());
    }

    private List<Element<GeneralApplication>> addApplication(GeneralApplication application,
                                                             List<Element<GeneralApplication>>
                                                                 generalApplicationDetails) {
        List<Element<GeneralApplication>> newApplication = ofNullable(generalApplicationDetails).orElse(newArrayList());
        newApplication.add(element(application));

        return newApplication;
    }

    public boolean respondentAssigned(CaseData caseData) {
        String caseId = caseData.getCcdCaseReference().toString();
        log.info("Checking if respondent is assigned to case: {}", caseId);
        CaseAssignmentUserRolesResource userRoles = getUserRolesOnCase(caseId);
        List<String> respondentCaseRoles = getRespondentCaseRoles(caseData);
        return isLipCase(caseData, userRoles) || areRespondentRolesAssigned(userRoles, respondentCaseRoles, caseId);
    }

    private boolean isLipCase(CaseData caseData, CaseAssignmentUserRolesResource userRoles) {
        log.info("Checking isLipCase for case: {}", caseData.getCcdCaseReference());
        if (caseData.isRespondent1LiP() || caseData.isRespondent2LiP()
            || caseData.isApplicantNotRepresented()) {

            for (String lipRole : lipCaseRole) {
                if (userRoles.getCaseAssignmentUserRoles() != null && userRoles.getCaseAssignmentUserRoles().size() > 1
                    || userRoles.getCaseAssignmentUserRoles().stream()
                    .anyMatch(role -> role.getCaseRole().equals(lipRole))) {
                    log.info("Checking isLipCase 111 case: {}", caseData.getCcdCaseReference());
                    return true;
                }
            }
            log.info("Checking isLipCase 222 case: {}", caseData.getCcdCaseReference());
            return false;
        }
        log.info("Checking isLipCase 333 case: {}", caseData.getCcdCaseReference());
        return false;
    }

    private boolean areRespondentRolesAssigned(CaseAssignmentUserRolesResource userRoles, List<String> respondentCaseRoles, String caseId) {
        log.info("Checking areRespondentRolesAssigned for case: {}", caseId);
        for (String respondentCaseRole : respondentCaseRoles) {
            if (userRoles.getCaseAssignmentUserRoles() == null
                || userRoles.getCaseAssignmentUserRoles().stream()
                .noneMatch(a -> a.getCaseRole() != null && respondentCaseRole.equals(a.getCaseRole()))) {
                log.info("Checking areRespondentRolesAssigned  11 for case: {}", caseId);
                return false;
            }
        }
        log.info("Checking areRespondentRolesAssigned  22 for case: {}", caseId);
        return true;
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

    private String getClaimTrackForTaskName(CaseData caseData) {
        String taskTrackName = determineTaskTrackName(caseData);
        return mapTaskTrackNameToLabel(taskTrackName);
    }

    private String mapTaskTrackNameToLabel(String taskTrackName) {
        return switch (taskTrackName) {
            case "MULTI_CLAIM" -> MULTI_CLAIM_TRACK.getValue();
            case "INTERMEDIATE_CLAIM" -> INTERMEDIATE_CLAIM_TRACK.getValue();
            case "SMALL_CLAIM" -> SMALL_CLAIM_TRACK.getValue();
            case "FAST_CLAIM" -> FAST_CLAIM_TRACK.getValue();
            default -> " ";
        };
    }

    private String determineTaskTrackName(CaseData caseData) {
        if (nonNull(caseData.getAllocatedTrack()) && UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return caseData.getAllocatedTrack().name();
        } else if (nonNull(caseData.getResponseClaimTrack()) && SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return caseData.getResponseClaimTrack();
        }
        return "NO_TASK_TRACK_FOUND";
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

    private boolean hasSDOBeenMade(CaseData caseData) {
        return (!statesBeforeSDO.contains(caseData.getCcdState())
            && !settleDiscontinueStates.contains(caseData.getCcdState()))
            || (!statesBeforeSDO.contains(caseData.getPreviousCCDState())
            && settleDiscontinueStates.contains(caseData.getCcdState()));
    }

    private void setGeneralAppEvidenceDocument(CaseData caseData, GeneralApplication application) {
        if (caseData.getGeneralAppEvidenceDocument() != null) {
            application.setGeneralAppEvidenceDocument(caseData.getGeneralAppEvidenceDocument());
        }
    }

    private void setMultiPartyScenario(CaseData caseData, GeneralApplication application) {
        application.setIsMultiParty(MultiPartyScenario.isMultiPartyScenario(caseData) ? YES : NO);
    }

    private void setPartyNames(CaseData caseData, GeneralApplication application) {
        application.setEmailPartyReference(buildPartiesReferencesEmailSubject(caseData));
        application.setClaimant1PartyName(getPartyNameBasedOnType(caseData.getApplicant1()));
        application.setDefendant1PartyName(getPartyNameBasedOnType(caseData.getRespondent1()));
        if (YES.equals(caseData.getAddApplicant2())) {
            application.setClaimant2PartyName(getPartyNameBasedOnType(caseData.getApplicant2()));
        }
        if (YES.equals(caseData.getAddRespondent2())) {
            application.setDefendant2PartyName(getPartyNameBasedOnType(caseData.getRespondent2()));
        }
    }

    private void setCaseType(CaseData caseData, GeneralApplication application) {
        final var caseType = SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) ? CaseCategory.SPEC_CLAIM : CaseCategory.UNSPEC_CLAIM;
        application.setGeneralAppSuperClaimType(caseType.name()).setCaseAccessCategory(caseType);
    }

    private void setDates(CaseData caseData, GeneralApplication application) {
        application.setMainCaseSubmittedDate(caseData.getSubmittedDate());
    }

    private void setRespondentAgreement(CaseData caseData, GeneralApplication application) {
        if (caseData.getGeneralAppRespondentAgreement() != null) {
            if (YES.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed())
                && !caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)) {
                GAStatementOfTruth gaStatementOfTruth = ofNullable(caseData.getGeneralAppStatementOfTruth())
                    .orElseGet(GAStatementOfTruth::new);
                GAInformOtherParty informOtherParty = new GAInformOtherParty();
                informOtherParty.setIsWithNotice(YES);
                application
                    .setGeneralAppInformOtherParty(informOtherParty)
                    .setGeneralAppConsentOrder(NO)
                    .setGeneralAppStatementOfTruth(gaStatementOfTruth);
            } else {
                application
                    .setGeneralAppInformOtherParty(caseData.getGeneralAppInformOtherParty())
                    .setGeneralAppStatementOfTruth(caseData.getGeneralAppStatementOfTruth());
            }
        } else {
            application
                .setGeneralAppInformOtherParty(new GAInformOtherParty())
                .setGeneralAppStatementOfTruth(new GAStatementOfTruth());
        }
    }

    private void setCaseManagementCategory(GeneralApplication application) {
        GACaseManagementCategoryElement civil = new GACaseManagementCategoryElement();
        civil.setCode("Civil");
        civil.setLabel("Civil");
        List<Element<GACaseManagementCategoryElement>> itemList = new ArrayList<>();
        itemList.add(element(civil));
        GACaseManagementCategory caseManagementCategory = new GACaseManagementCategory();
        caseManagementCategory.setValue(civil);
        caseManagementCategory.setList_items(itemList);
        application.setCaseManagementCategory(caseManagementCategory);
    }

    private void setCaseManagementLocation(CaseData caseData, String authToken, GeneralApplication application) {
        if (caseContainsLiP(caseData) && hasSDOBeenMade(caseData)) {
            setLocationDetails(caseData, authToken, application);
        } else {
            setDefaultLocation(caseData, authToken, application);
        }
    }

    private void setLocationDetails(CaseData caseData, String authToken, GeneralApplication application) {
        LocationRefData locationDetails = locationService.getWorkAllocationLocationDetails(caseData.getCaseManagementLocation().getBaseLocation(), authToken);
        CaseLocationCivil caseManagementLocation = new CaseLocationCivil();
        caseManagementLocation.setRegion(caseData.getCaseManagementLocation().getRegion());
        caseManagementLocation.setBaseLocation(caseData.getCaseManagementLocation().getBaseLocation());
        caseManagementLocation.setSiteName(locationDetails.getSiteName());
        caseManagementLocation.setAddress(locationDetails.getCourtAddress());
        caseManagementLocation.setPostcode(locationDetails.getPostcode());
        application.setCaseManagementLocation(caseManagementLocation);
        application.setLocationName(locationDetails.getSiteName());
        application.setIsCcmccLocation(NO);
    }

    private void setDefaultLocation(CaseData caseData, String authToken, GeneralApplication application) {
        Pair<CaseLocationCivil, Boolean> caseLocation = locationService.getWorkAllocationLocation(caseData, authToken);
        if (Objects.isNull(caseLocation.getLeft().getBaseLocation()) && !caseLocation.getRight()) {
            caseLocation.getLeft().setBaseLocation(caseData.getCaseManagementLocation().getBaseLocation());
            caseLocation.getLeft().setRegion(caseData.getCaseManagementLocation().getRegion());
        }
        if (Objects.isNull(caseLocation.getLeft().getSiteName()) && Objects.nonNull(caseLocation.getLeft().getBaseLocation())) {
            LocationRefData locationDetails = locationService.getWorkAllocationLocationDetails(caseLocation.getLeft().getBaseLocation(), authToken);
            caseLocation.getLeft().setSiteName(locationDetails.getSiteName());
            caseLocation.getLeft().setAddress(locationDetails.getCourtAddress());
            caseLocation.getLeft().setPostcode(locationDetails.getPostcode());
        }
        application.setCaseManagementLocation(caseLocation.getLeft());
        application.setLocationName(hasSDOBeenMade(caseData) ? caseData.getLocationName() : caseLocation.getLeft().getSiteName());
        application.setIsCcmccLocation(caseLocation.getRight() ? YES : NO);
    }

    private void setGeneralAppN245FormUpload(CaseData caseData, GeneralApplication application) {
        if (caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)
            && !Objects.isNull(caseData.getGeneralAppN245FormUpload())) {
            if (Objects.isNull(caseData.getGeneralAppN245FormUpload().getCategoryID())) {
                caseData.getGeneralAppN245FormUpload().setCategoryID(GA_DOC_CATEGORY_ID.getValue());
            }
            application.setGeneralAppN245FormUpload(caseData.getGeneralAppN245FormUpload());
            List<Element<Document>> gaEvidenceDoc = ofNullable(caseData.getGeneralAppEvidenceDocument()).orElse(newArrayList());
            gaEvidenceDoc.add(element(caseData.getGeneralAppN245FormUpload()));
            application.setGeneralAppEvidenceDocument(gaEvidenceDoc);
        }
    }

    private void setBusinessProcess(CaseData caseData, UserDetails userDetails, GeneralApplication application) {
        LocalDateTime deadline = null;
        if (!(featureToggleService.isGaForWelshEnabled()
            && (caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual()))) {
            int numberOfDeadlineDays = 5;
            deadline = deadlinesCalculator.calculateApplicantResponseDeadline(LocalDateTime.now(), numberOfDeadlineDays);
        }
        application
            .setBusinessProcess(BusinessProcess.ready(INITIATE_GENERAL_APPLICATION))
            .setGeneralAppType(caseData.getGeneralAppType())
            .setGeneralAppHearingDate(caseData.getGeneralAppHearingDate())
            .setGeneralAppRespondentAgreement(caseData.getGeneralAppRespondentAgreement())
            .setGeneralAppUrgencyRequirement(caseData.getGeneralAppUrgencyRequirement())
            .setGeneralAppDetailsOfOrder(caseData.getGeneralAppDetailsOfOrder())
            .setGeneralAppDetailsOfOrderColl(caseData.getGeneralAppDetailsOfOrderColl())
            .setGeneralAppReasonsOfOrder(caseData.getGeneralAppReasonsOfOrder())
            .setGeneralAppReasonsOfOrderColl(caseData.getGeneralAppReasonsOfOrderColl())
            .setGeneralAppHearingDetails(caseData.getGeneralAppHearingDetails())
            .setGeneralAppHelpWithFees(caseData.getGeneralAppHelpWithFees())
            .setGeneralAppPBADetails(caseData.getGeneralAppPBADetails())
            .setGeneralAppAskForCosts(caseData.getGeneralAppAskForCosts())
            .setGeneralAppDateDeadline(deadline)
            .setGeneralAppSubmittedDateGAspec(LocalDateTime.now())
            .setCivilServiceUserRoles(createIdamUserDetails(userDetails));
    }

    private IdamUserDetails createIdamUserDetails(UserDetails userDetails) {
        IdamUserDetails idamUserDetails = new IdamUserDetails();
        idamUserDetails.setId(userDetails.getId());
        idamUserDetails.setEmail(userDetails.getEmail());
        return idamUserDetails;
    }

    private void setFeatureToggles(CaseData caseData, GeneralApplication application) {
        application.setIsGaApplicantLip(NO).setIsGaRespondentOneLip(NO).setIsGaRespondentTwoLip(NO);
        if (caseData.isRespondent1LiP() || caseData.isRespondent2LiP() || caseData.isApplicantNotRepresented()) {
            application.setGeneralAppSubmittedDateGAspec(time.now());
        }
        if (caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID)) {
            if (Objects.nonNull(caseData.getCertOfSC().getDebtPaymentEvidence()) && Objects.nonNull(caseData.getCertOfSC().getDebtPaymentEvidence().getDebtPaymentOption())) {
                DebtPaymentOptions deptPaymentOption = caseData.getCertOfSC().getDebtPaymentEvidence().getDebtPaymentOption();
                if (DebtPaymentOptions.MADE_FULL_PAYMENT_TO_COURT.equals(deptPaymentOption) || DebtPaymentOptions.UPLOAD_EVIDENCE_DEBT_PAID_IN_FULL.equals(deptPaymentOption)) {
                    caseData.getCertOfSC().setProofOfDebtDoc(caseData.getGeneralAppEvidenceDocument());
                } else {
                    caseData.getCertOfSC().setProofOfDebtDoc(java.util.Collections.emptyList());
                }
            }
            application.setCertOfSC(caseData.getCertOfSC());
        }
        application.setGaWaTrackLabel(getClaimTrackForTaskName(caseData));
    }

}
