package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_WITH_GA_STATE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_DIRECTIONS_ORDER_DOCS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class ParentCaseUpdateHelper {

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final FeatureToggleService featureToggleService;
    private final GaCaseDataEnricher gaCaseDataEnricher;
    private final ObjectMapper mapper;

    private static final Logger log = LoggerFactory.getLogger(ParentCaseUpdateHelper.class);

    private static final String GENERAL_APPLICATIONS_DETAILS_FOR_CLAIMANT = "claimantGaAppDetails";
    private static final String GENERAL_APPLICATIONS_DETAILS_FOR_RESP_SOL = "respondentSolGaAppDetails";
    private static final String GENERAL_APPLICATIONS_DETAILS_FOR_RESP_SOL_TWO = "respondentSolTwoGaAppDetails";
    private static final String GENERAL_APPLICATIONS_DETAILS_FOR_JUDGE = "gaDetailsMasterCollection";
    private static final String GENERAL_APPLICATIONS_DETAILS_FOR_WELSH = "gaDetailsTranslationCollection";
    private static final String GA_DRAFT_FORM = "gaDraft";
    private static final String[] DOCUMENT_TYPES = {
        "generalOrder", "dismissalOrder",
        "directionOrder", "hearingNotice",
        "gaResp", GA_DRAFT_FORM, "gaAddl"
    };
    private static final String CLAIMANT_ROLE = "Claimant";
    private static final String RESPONDENTSOL_ROLE = "RespondentSol";
    private static final String RESPONDENTSOL_TWO_ROLE = "RespondentSolTwo";
    private String[] roles = {CLAIMANT_ROLE, RESPONDENTSOL_ROLE, RESPONDENTSOL_TWO_ROLE};
    private static final String GA_EVIDENCE = "gaEvidence";
    private static final String GA_ADDL = "gaAddl";
    private static final String CIVIL_GA_EVIDENCE = "generalAppEvidence";
    private static final String FREE_KEYWORD = "FREE";

    protected static List<CaseState> DOCUMENT_STATES = Arrays.asList(
            AWAITING_ADDITIONAL_INFORMATION,
            AWAITING_WRITTEN_REPRESENTATIONS,
            AWAITING_DIRECTIONS_ORDER_DOCS,
            PENDING_APPLICATION_ISSUED,
            APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION
    );

    public void updateParentWithGAState(GeneralApplicationCaseData gaCaseData, String newState) {

        String applicationId = gaCaseData.getCcdCaseReference().toString();
        String parentCaseId = gaCaseData.getGeneralAppParentCaseLink().getCaseReference();
        String[] docVisibilityRoles = new String[4];

        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(parentCaseId,
                                                                                UPDATE_CASE_WITH_GA_STATE);
        CaseData caseData = caseDetailsConverter.toCaseDataGA(startEventResponse.getCaseDetails());

        List<Element<GADetailsRespondentSol>> respondentSpecficGADetails =
            ofNullable(caseData.getRespondentSolGaAppDetails()).orElse(newArrayList());

        if (!isEmpty(respondentSpecficGADetails)) {
            /*
            * Check if the application exists in the respondentSpecficGADetails List which matches the applicationId
            * as the current application with applicationId may not present in the respondentSpecficGADetails List
            * due to requirement.
            *
            * Requirement - A Without Notice application should be hidden from any Legal Reps other than the Applicant
            *  */
            if (respondentSpecficGADetails.stream()
                .anyMatch(gaRespondentApp -> gaRespSolAppFilterCriteria(gaRespondentApp, applicationId))) {

                respondentSpecficGADetails.stream()
                    .filter(gaRespondentApp -> gaRespSolAppFilterCriteria(gaRespondentApp, applicationId))
                    .findAny().orElseThrow(IllegalArgumentException::new).getValue().setCaseState(newState);
                docVisibilityRoles[0] = RESPONDENTSOL_ROLE;
            }
        }

        List<Element<GADetailsRespondentSol>> respondentSpecficGADetailsTwo =
            ofNullable(caseData.getRespondentSolTwoGaAppDetails()).orElse(newArrayList());

        if (!isEmpty(respondentSpecficGADetailsTwo)) {
            /*
             * Check if the application exists in the respondent two List which matches the applicationId
             * as the current application with applicationId may not present in the respondentSpecficGADetailsTwo List
             * due to requirement.
             *
             * Requirement - A Without Notice application should be hidden from any Legal Reps other than the Applicant
             *  */
            if (respondentSpecficGADetailsTwo.stream()
                .anyMatch(gaRespondentApp -> gaRespSolAppFilterCriteria(gaRespondentApp, applicationId))) {

                respondentSpecficGADetailsTwo.stream()
                    .filter(gaRespondentApp -> gaRespSolAppFilterCriteria(gaRespondentApp, applicationId))
                    .findAny().orElseThrow(IllegalArgumentException::new).getValue().setCaseState(newState);
                docVisibilityRoles[1] = RESPONDENTSOL_TWO_ROLE;
            }
        }

        /*
         * Check if the application exists in the main claim claimant List which matches the applicationId
         * as the current application with applicationId may not present in the Claimant List
         * due to requirement.
         *
         * Requirement - A Without Notice application should be hidden from any Legal Reps other than the Applicant
         * e.g Main claim defendant initiate the GA without notice which should be hidden to main claim claimant
         * unless judge uncloak it
         *  */
        List<Element<GeneralApplicationsDetails>> generalApplications = updateGaApplicationState(
            caseData,
            newState,
            applicationId,
            docVisibilityRoles
        );

        /*
         * Check if the application exists in the Judge List which matches the applicationId
         *  */
        List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection = updateJudgeGaApplicationState(
            caseData,
            newState,
            applicationId
        );
        docVisibilityRoles[3] = "Staff";

        CaseData generalAppCaseData = toCaseData(gaCaseData);
        List<Element<GeneralApplication>> civilGeneralApplications = caseData.getGeneralApplications();

        if (generalAppCaseData.getCcdState().equals(PENDING_APPLICATION_ISSUED) && !isEmpty(civilGeneralApplications)) {
            List<Element<GeneralApplication>> generalApplicationsList = civilGeneralApplications.stream()
                .filter(app -> app.getValue().getCaseLink() != null && !app.getValue().getCaseLink().getCaseReference().equals(
                    applicationId))
                .toList();
            Optional<Element<GeneralApplication>> newApplicationElement = civilGeneralApplications.stream()
                .filter(app -> app.getValue().getCaseLink() != null && app.getValue().getCaseLink().getCaseReference().equals(
                    applicationId))
                .findFirst();
            GeneralApplication generalApplication = civilGeneralApplications.stream()
                .filter(app -> app.getValue().getCaseLink() != null && app.getValue().getCaseLink().getCaseReference().equals(
                    applicationId))
                .findAny()
                .orElseThrow(IllegalArgumentException::new)
                .getValue();

            civilGeneralApplications =
                addApplication(
                    newApplicationElement,
                    buildGeneralApplication(generalApplication),
                    generalApplicationsList
                );
        }

        Map<String, Object> updateMap = getUpdatedCaseData(caseData, civilGeneralApplications, generalApplications,
                respondentSpecficGADetails,
                respondentSpecficGADetailsTwo,
                gaDetailsMasterCollection);
        if (DOCUMENT_STATES.contains(generalAppCaseData.getCcdState())) {
            updateCaseDocument(updateMap, caseData, generalAppCaseData, docVisibilityRoles);
        }
        if ((Objects.nonNull(generalAppCaseData.getGeneralAppEvidenceDocument())
            && !generalAppCaseData.getGeneralAppEvidenceDocument().isEmpty())
            || (Objects.nonNull(generalAppCaseData.getGaDraftDocument())
            && !generalAppCaseData.getGaDraftDocument().isEmpty())) {
            updateEvidence(updateMap, caseData, generalAppCaseData, docVisibilityRoles);
        }
        coreCaseDataService.submitUpdate(parentCaseId, coreCaseDataService.caseDataContentFromStartEventResponse(
            startEventResponse, updateMap));
    }

    public void updateParentWithGAState(CaseData caseData, String newState) {
        updateParentWithGAState(toGaCaseData(caseData), newState);
    }

    protected void updateEvidence(Map<String, Object> updateMap, CaseData civilCaseData,
                                  CaseData generalAppCaseData, String[] docVisibilityRoles) {
        String[] evidenceRole = null;
        if (generalAppCaseData.getCcdState().equals(PENDING_APPLICATION_ISSUED)) {
            log.info("PENDING_APPLICATION_ISSUED for Case ID: {}", generalAppCaseData.getCcdCaseReference());
            String[] evidenceRoleBefore = new String[1];
            evidenceRoleBefore[0] = findGaCreator(civilCaseData, generalAppCaseData);
            evidenceRole = evidenceRoleBefore;
        } else if (generalAppCaseData.getCcdState().equals(AWAITING_APPLICATION_PAYMENT)) {
            log.info("AWAITING_APPLICATION_PAYMENT for Case ID: {}", generalAppCaseData.getCcdCaseReference());
            evidenceRole = docVisibilityRoles;
        }
        if (Objects.nonNull(evidenceRole)) {
            updateSingleTypeByRoles(updateMap, GA_EVIDENCE, evidenceRole,
                    civilCaseData, generalAppCaseData);
        }
    }

    protected void updateSingleTypeByRoles(Map<String, Object> updateMap, String type, String[] roles,
                                         CaseData civilCaseData, CaseData generalAppCaseData) {
        for (String role : roles) {
            try {
                updateCaseDocumentByType(updateMap, type, role, civilCaseData, generalAppCaseData);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    protected String findGaCreator(CaseData civilCaseData, CaseData generalAppCaseData) {
        log.info("Starting findGaCreator. Evaluating GA creator for Application ID: {}", generalAppCaseData.getCcdCaseReference());
        if (generalAppCaseData.getParentClaimantIsApplicant().equals(YES)) {
            return CLAIMANT_ROLE;
        }
        if (featureToggleService.isGaForLipsEnabled()
            && generalAppCaseData.getIsGaApplicantLip() == YES) {
            return RESPONDENTSOL_ROLE;
        }
        String creatorId = generalAppCaseData.getGeneralAppApplnSolicitor().getOrganisationIdentifier();
        String respondent1OrganisationId = civilCaseData.getRespondent1OrganisationPolicy().getOrganisation()
                != null ? civilCaseData.getRespondent1OrganisationPolicy().getOrganisation()
                .getOrganisationID() : civilCaseData.getRespondent1OrganisationIDCopy();
        log.debug("GA creator Organisation ID: {}, Respondent 1 Organisation ID: {}", creatorId, respondent1OrganisationId);
        if (creatorId
                .equals(respondent1OrganisationId)) {
            log.info("GA creator is Respondent Solicitor 1.");
            return RESPONDENTSOL_ROLE;
        }
        String respondent2OrganisationId = civilCaseData.getRespondent2OrganisationPolicy().getOrganisation()
                != null ? civilCaseData.getRespondent2OrganisationPolicy().getOrganisation()
                .getOrganisationID() : civilCaseData.getRespondent2OrganisationIDCopy();
        if (generalAppCaseData.getIsMultiParty().equals(YES) && civilCaseData.getAddApplicant2().equals(NO)
                && civilCaseData.getRespondent2SameLegalRepresentative().equals(NO)
                && creatorId
                .equals(respondent2OrganisationId)) {
            log.info("GA creator is Respondent Solicitor 2.");
            return RESPONDENTSOL_TWO_ROLE;
        }
        return null;
    }

    public void updateMasterCollectionForHwf(GeneralApplicationCaseData gaCaseData) {

        CaseData generalAppCaseData = toCaseData(gaCaseData);

        String parentCaseId = gaCaseData.getGeneralAppParentCaseLink().getCaseReference();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
            parentCaseId,
            UPDATE_CASE_WITH_GA_STATE
        );

        CaseData parentCaseData = caseDetailsConverter.toCaseDataGA(startEventResponse.getCaseDetails());
        String applicationId = gaCaseData.getCcdCaseReference().toString();

        List<Element<GeneralApplicationsDetails>> gaClaimantDetails = ofNullable(
            parentCaseData.getClaimantGaAppDetails()).orElse(newArrayList());

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol2 = ofNullable(
            parentCaseData.getRespondentSolTwoGaAppDetails()).orElse(newArrayList());

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol = ofNullable(
            parentCaseData.getRespondentSolGaAppDetails()).orElse(newArrayList());

        List<Element<GeneralApplicationsDetails>> gaMasterDetails = ofNullable(
            parentCaseData.getGaDetailsMasterCollection()).orElse(newArrayList());

        if (generalAppCaseData.getParentClaimantIsApplicant().equals(YES)) {
            Optional<Element<GeneralApplicationsDetails>> claimantCollection = gaClaimantDetails
                .stream().filter(claimantApp -> applicationFilterCriteria(claimantApp, applicationId)).findAny();

            Optional<Element<GeneralApplicationsDetails>> masterCollection = gaMasterDetails
                .stream().filter(masterCollectionElement -> applicationFilterCriteria(
                    masterCollectionElement,
                    applicationId
                )).findAny();

            claimantCollection.ifPresent(generalApplicationsDetailsElement -> {
                if (masterCollection.isEmpty()) {
                    gaMasterDetails.add(
                        element(
                            GeneralApplicationsDetails.builder()
                                .generalApplicationType(generalApplicationsDetailsElement.getValue().getGeneralApplicationType())
                                .generalAppSubmittedDateGAspec(generalApplicationsDetailsElement.getValue()
                                                                   .getGeneralAppSubmittedDateGAspec())
                                .caseLink(CaseLink.builder().caseReference(String.valueOf(
                                    generalAppCaseData.getCcdCaseReference())).build()).build()));
                }
            });
        } else {

            updateJudgeOrClaimantFromRespCollection(
                generalAppCaseData,
                applicationId,
                gaMasterDetails,
                gaDetailsRespondentSol
            );
        }

        Map<String, Object> updateMap = getUpdatedCaseData(parentCaseData, parentCaseData.getGeneralApplications(),
                                                           gaClaimantDetails,
                                                           gaDetailsRespondentSol,
                                                           gaDetailsRespondentSol2,
                                                           gaMasterDetails
        );
        removeApplicationFromTranslationCollection(parentCaseData, updateMap, applicationId);

        CaseDataContent caseDataContent = coreCaseDataService.caseDataContentFromStartEventResponse(
            startEventResponse, updateMap);

        coreCaseDataService.submitUpdate(parentCaseId, caseDataContent);
    }

    public void updateMasterCollectionForHwf(CaseData caseData) {
        updateMasterCollectionForHwf(toGaCaseData(caseData));
    }

    public void updateJudgeAndRespondentCollectionAfterPayment(GeneralApplicationCaseData gaCaseData) {

        CaseData generalAppCaseData = toCaseData(gaCaseData);

        String applicationId = gaCaseData.getCcdCaseReference().toString();
        String parentCaseId = gaCaseData.getGeneralAppParentCaseLink().getCaseReference();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
            parentCaseId,
            UPDATE_CASE_WITH_GA_STATE
        );
        CaseData parentCaseData = caseDetailsConverter.toCaseDataGA(startEventResponse.getCaseDetails());

        List<Element<GeneralApplicationsDetails>> gaMasterDetails = ofNullable(
            parentCaseData.getGaDetailsMasterCollection()).orElse(newArrayList());

        List<Element<GeneralApplicationsDetails>> gaClaimantDetails = ofNullable(
            parentCaseData.getClaimantGaAppDetails()).orElse(newArrayList());

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol = ofNullable(
            parentCaseData.getRespondentSolGaAppDetails()).orElse(newArrayList());

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol2 = ofNullable(
            parentCaseData.getRespondentSolTwoGaAppDetails()).orElse(newArrayList());

        if (generalAppCaseData.getParentClaimantIsApplicant().equals(YES)) {
            addClaimantApplicationDetails(generalAppCaseData, applicationId, gaMasterDetails, gaClaimantDetails);
            /**
             * When main claim's 1 V 2 Same Legal Representative happens,
             * Check if main claim "Respondent2SameLegalRespresentative" value is true,
             * if so, ADD GA application has to master collection
             *
             * In addition to above, the condition : generalAppCaseData.getIsMultiParty().equals(NO)
             * Add GA into mater collection if it's not multiparty scenario and GA initiated by Main claim Defendant 1v1
             */
        } else if ((Objects.nonNull(parentCaseData.getRespondent2SameLegalRepresentative())
            && parentCaseData.getRespondent2SameLegalRepresentative().equals(YES))
            || generalAppCaseData.getIsMultiParty().equals(NO)) {

            updateJudgeOrClaimantFromRespCollection(
                generalAppCaseData,
                applicationId,
                gaMasterDetails,
                gaDetailsRespondentSol
            );
        }

        if (generalAppCaseData.getIsMultiParty().equals(YES)
            && !gaDetailsRespondentSol.isEmpty()) {
            updateJudgeOrClaimantFromRespCollection(generalAppCaseData, applicationId, gaMasterDetails, gaDetailsRespondentSol);
        }

        if (generalAppCaseData.getIsMultiParty().equals(YES)
            && !gaDetailsRespondentSol2.isEmpty()) {
            updateJudgeOrClaimantFromRespCollection(generalAppCaseData, applicationId, gaMasterDetails, gaDetailsRespondentSol2);
        }

        /**
         * Respondent Agreement is NO and with notice.
         * Application should be visible to all solicitor
         * Consent order should be visible to all solicitors
         */
        if ((generalAppCaseData.getGeneralAppRespondentAgreement().getHasAgreed().equals(NO)
            && ofNullable(generalAppCaseData.getGeneralAppInformOtherParty()).isPresent()
            && YES.equals(generalAppCaseData.getGeneralAppInformOtherParty().getIsWithNotice()))
            || generalAppCaseData.getGeneralAppRespondentAgreement().getHasAgreed().equals(YES)) {

            if (generalAppCaseData.getParentClaimantIsApplicant().equals(YES)) {
                updateRespCollectionFromClaimant(generalAppCaseData, applicationId, gaDetailsRespondentSol, gaClaimantDetails);
                if (generalAppCaseData.getIsMultiParty().equals(YES)) {
                    updateRespCollectionFromClaimant(generalAppCaseData, applicationId, gaDetailsRespondentSol2, gaClaimantDetails);
                }
            } else {
                if ((Objects.nonNull(parentCaseData.getRespondent2SameLegalRepresentative())
                    && parentCaseData.getRespondent2SameLegalRepresentative().equals(YES))
                    || generalAppCaseData.getIsMultiParty().equals(NO)) {

                    /**
                     * When main claim's 1 V 2 Same Legal Representative happens,
                     * Check if main claim "Respondent2SameLegalRespresentative" value is true,
                     * if so, ADD GA application has to master collection
                     *
                     * In addition to above, above condition, Add GA into mater collection if it's not multiparty scenario
                     */
                    updateJudgeOrClaimantFromRespCollection(generalAppCaseData, applicationId, gaClaimantDetails, gaDetailsRespondentSol);

                }
            }

            /**
             * Parties : Claimant, Respondent 1, Respondent 2
             *
             * Condition : Multiparty - Yes, Respondent One initiates the GA - Yes
             * Add GA from Respondent One Collection into Claimant's and Respondent Two's collections
             */
            if (generalAppCaseData.getIsMultiParty().equals(YES) && !gaDetailsRespondentSol.isEmpty()) {
                log.info("Multiparty case and Respondent One initiates the GA for Case ID: {}", generalAppCaseData.getCcdCaseReference());
                updateJudgeOrClaimantFromRespCollection(generalAppCaseData, applicationId, gaClaimantDetails, gaDetailsRespondentSol);
                updateRespCollectionForMultiParty(generalAppCaseData, applicationId, gaDetailsRespondentSol2, gaDetailsRespondentSol);
            }

            /**
             * Parties : Claimant, Respondent 1, Respondent 2
             *
             * Condition : Multiparty - Yes, Respondent Two initiates the GA - Yes
             * Add GA from Respondent Two Collection into Claimant's and Respondent One's collections
             */
            if (generalAppCaseData.getIsMultiParty().equals(YES) && !gaDetailsRespondentSol2.isEmpty()) {
                log.info("Multiparty case and Respondent Two initiates the GA for Case ID: {}", generalAppCaseData.getCcdCaseReference());
                updateJudgeOrClaimantFromRespCollection(generalAppCaseData, applicationId, gaClaimantDetails, gaDetailsRespondentSol2);
                updateRespCollectionForMultiParty(generalAppCaseData, applicationId, gaDetailsRespondentSol, gaDetailsRespondentSol2);
            }

        }

        Map<String, Object> updateMap = getUpdatedCaseData(parentCaseData, parentCaseData.getGeneralApplications(),
                                                           gaClaimantDetails,
                                                           gaDetailsRespondentSol,
                                                           gaDetailsRespondentSol2,
                                                           gaMasterDetails);
        removeApplicationFromTranslationCollection(parentCaseData, updateMap, applicationId);
        CaseDataContent caseDataContent = coreCaseDataService.caseDataContentFromStartEventResponse(
            startEventResponse, updateMap);

        coreCaseDataService.submitUpdate(parentCaseId, caseDataContent);
    }

    public void updateJudgeAndRespondentCollectionAfterPayment(CaseData caseData) {
        updateJudgeAndRespondentCollectionAfterPayment(toGaCaseData(caseData));
    }

    public void updateCollectionForWelshApplication(GeneralApplicationCaseData gaCaseData) {
        CaseData generalAppCaseData = toCaseData(gaCaseData);
        String applicationId = gaCaseData.getCcdCaseReference().toString();
        String parentCaseId = gaCaseData.getGeneralAppParentCaseLink().getCaseReference();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
            parentCaseId,
            UPDATE_CASE_WITH_GA_STATE
        );
        CaseData parentCaseData = caseDetailsConverter.toCaseDataGA(startEventResponse.getCaseDetails());
        List<Element<GeneralApplicationsDetails>> gaTranslationDetails = ofNullable(
            parentCaseData.getGaDetailsTranslationCollection()).orElse(newArrayList());

        if (generalAppCaseData.getParentClaimantIsApplicant().equals(YES)) {
            List<Element<GeneralApplicationsDetails>> gaClaimantDetails = ofNullable(
                parentCaseData.getClaimantGaAppDetails()).orElse(newArrayList());
            addClaimantApplicationDetails(generalAppCaseData, applicationId, gaTranslationDetails, gaClaimantDetails);
        } else {
            List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol = ofNullable(
                parentCaseData.getRespondentSolGaAppDetails()).orElse(newArrayList());
            updateJudgeOrClaimantFromRespCollection(
                generalAppCaseData,
                applicationId,
                gaTranslationDetails,
                gaDetailsRespondentSol
            );
        }
        Map<String, Object> updateMap = getUpdateCaseDataForCollection(parentCaseData, gaTranslationDetails);
        CaseDataContent caseDataContent = coreCaseDataService.caseDataContentFromStartEventResponse(
            startEventResponse, updateMap);

        coreCaseDataService.submitUpdate(parentCaseId, caseDataContent);
    }

    public void updateCollectionForWelshApplication(CaseData caseData) {
        updateCollectionForWelshApplication(toGaCaseData(caseData));
    }

    private void addClaimantApplicationDetails(CaseData generalAppCaseData, String applicationId,
                                               List<Element<GeneralApplicationsDetails>> gaTranslationDetails,
                                               List<Element<GeneralApplicationsDetails>> gaClaimantDetails) {
        Optional<Element<GeneralApplicationsDetails>> claimantCollection = gaClaimantDetails
            .stream().filter(claimantApp -> applicationFilterCriteria(claimantApp, applicationId)).findAny();
        claimantCollection.ifPresent(generalApplicationsDetailsElement -> gaTranslationDetails.add(
            element(
                GeneralApplicationsDetails.builder()
                    .generalApplicationType(generalApplicationsDetailsElement.getValue().getGeneralApplicationType())
                    .generalAppSubmittedDateGAspec(generalApplicationsDetailsElement.getValue()
                                                       .getGeneralAppSubmittedDateGAspec())
                    .caseLink(CaseLink.builder().caseReference(String.valueOf(
                        generalAppCaseData.getCcdCaseReference())).build())
                    .parentClaimantIsApplicant(generalApplicationsDetailsElement.getValue()
                                                   .getParentClaimantIsApplicant())
                    .build())));
    }

    private void removeApplicationFromTranslationCollection(CaseData parentCaseData, Map<String, Object> updateMap,
                                                            String applicationId) {
        if (featureToggleService.isGaForWelshEnabled()) {
            List<Element<GeneralApplicationsDetails>> gaDetailsTranslationCollection = ofNullable(
                parentCaseData.getGaDetailsTranslationCollection()).orElse(newArrayList());

            if (!gaDetailsTranslationCollection.isEmpty()) {

                gaDetailsTranslationCollection.removeIf(
                    gaApplication -> applicationFilterCriteria(gaApplication, applicationId)
                );
                var data = gaDetailsTranslationCollection.isEmpty() ? " " : gaDetailsTranslationCollection;
                updateMap.put(GENERAL_APPLICATIONS_DETAILS_FOR_WELSH, data);
            }
        }
    }

    private void updateRespCollectionForMultiParty(CaseData generalAppCaseData, String applicationId,
                                                  List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol,
                                                  List<Element<GADetailsRespondentSol>> gaRespondentSol) {
        Optional<Element<GADetailsRespondentSol>> respCollection = gaRespondentSol
            .stream().filter(respCollectionApp -> applicationRespFilterCriteria(respCollectionApp, applicationId)).findAny();

        Optional<Element<GADetailsRespondentSol>> gaToBeAdded = gaDetailsRespondentSol
            .stream().filter(respCollectionElement -> gaRespSolAppFilterCriteria(respCollectionElement, applicationId)).findAny();

        /**
         * To Prevent duplicate, Check if the application already present in the Respondent Collection before adding it from another Collection
         */
        if (!gaToBeAdded.isPresent()) {
            respCollection.ifPresent(generalApplicationsDetailsElement -> gaDetailsRespondentSol.add(
                element(
                    GADetailsRespondentSol.builder()
                        .generalApplicationType(generalApplicationsDetailsElement.getValue().getGeneralApplicationType())
                        .generalAppSubmittedDateGAspec(generalApplicationsDetailsElement.getValue()
                                                           .getGeneralAppSubmittedDateGAspec())
                        .caseLink(CaseLink.builder().caseReference(String.valueOf(
                            generalAppCaseData.getCcdCaseReference())).build())
                        .parentClaimantIsApplicant(generalApplicationsDetailsElement.getValue().getParentClaimantIsApplicant())
                        .build())));
        }
    }

    private void updateRespCollectionFromClaimant(CaseData generalAppCaseData, String applicationId,
                                                  List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol,
                                                  List<Element<GeneralApplicationsDetails>> gaClaimantDetails) {

        Optional<Element<GeneralApplicationsDetails>> claimantCollection = gaClaimantDetails
            .stream().filter(claimantApp -> applicationFilterCriteria(claimantApp, applicationId)).findAny();
        claimantCollection.ifPresent(generalApplicationsDetailsElement -> gaDetailsRespondentSol.add(
            element(
                GADetailsRespondentSol.builder()
                    .generalApplicationType(generalApplicationsDetailsElement.getValue().getGeneralApplicationType())
                    .generalAppSubmittedDateGAspec(generalApplicationsDetailsElement.getValue()
                                                       .getGeneralAppSubmittedDateGAspec())
                    .caseLink(CaseLink.builder().caseReference(String.valueOf(
                        generalAppCaseData.getCcdCaseReference())).build())
                    .parentClaimantIsApplicant(generalApplicationsDetailsElement.getValue().getParentClaimantIsApplicant())
                    .build())));

    }

    private void updateJudgeOrClaimantFromRespCollection(CaseData generalAppCaseData, String applicationId,
                                                         List<Element<GeneralApplicationsDetails>> gaMasterDetails,
                                                         List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol) {
        if (!gaDetailsRespondentSol.isEmpty()) {

            Optional<Element<GADetailsRespondentSol>> respondentSolCollection = gaDetailsRespondentSol
                .stream().filter(respondentSolElement2 -> gaRespSolAppFilterCriteria(respondentSolElement2, applicationId)).findAny();

            Optional<Element<GeneralApplicationsDetails>> masterCollection = gaMasterDetails
                .stream().filter(masterCollectionElement -> applicationFilterCriteria(masterCollectionElement, applicationId)).findAny();

            /**
             * To Prevent duplicate, Check if the application already present in the Master Collection before adding it from Respondent Collection
             */
            if (!masterCollection.isPresent()) {
                log.info("Application with Case ID {} is added to respondent solicitor collection", generalAppCaseData.getCcdCaseReference());
                respondentSolCollection.ifPresent(respondentSolElement -> gaMasterDetails.add(
                    element(
                        GeneralApplicationsDetails.builder()
                            .generalApplicationType(respondentSolElement.getValue().getGeneralApplicationType())
                            .generalAppSubmittedDateGAspec(respondentSolElement.getValue()
                                                               .getGeneralAppSubmittedDateGAspec())
                            .caseLink(CaseLink.builder().caseReference(String.valueOf(
                                generalAppCaseData.getCcdCaseReference())).build())
                            .parentClaimantIsApplicant(respondentSolElement.getValue().getParentClaimantIsApplicant())
                            .build())));
            }
        }
    }

    public void updateParentApplicationVisibilityWithNewState(GeneralApplicationCaseData gaCaseData, String newState) {

        CaseData generalAppCaseData = toCaseData(gaCaseData);

        String applicationId = gaCaseData.getCcdCaseReference().toString();
        String parentCaseId = gaCaseData.getGeneralAppParentCaseLink().getCaseReference();
        log.info("Starting updateParentApplicationVisibilityWithNewState. New state: {}, Application ID: {}, Parent Case ID: {}", newState, applicationId, parentCaseId);

        StartEventResponse startEventResponse = coreCaseDataService
            .startUpdate(parentCaseId, UPDATE_CASE_WITH_GA_STATE);

        CaseData caseData = caseDetailsConverter.toCaseDataGA(startEventResponse.getCaseDetails());

        /*
        * check if the applicant exits in master collection Judge
        * */
        Optional<Element<GeneralApplicationsDetails>> generalApplicationsDetails = caseData
            .getGaDetailsMasterCollection()
            .stream().filter(application -> applicationFilterCriteria(application, applicationId)).findAny();

        if (generalApplicationsDetails.isPresent()) {

            /*
            * Respondent One Solicitor collection
            * */
            log.info("Found application in master collection with ID: {}", applicationId);
            List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol = ofNullable(
                caseData.getRespondentSolGaAppDetails()).orElse(newArrayList());

            boolean isGaDetailsRespondentSolPresent = gaDetailsRespondentSol.stream()
                .anyMatch(gaRespondentApp -> gaRespSolAppFilterCriteria(gaRespondentApp, applicationId));

            /*
            * Add the GA into Respondent one solicitor collection
            * */
            if (!isGaDetailsRespondentSolPresent) {
                log.info("Adding application to Respondent One Solicitor collection.");
                gaDetailsRespondentSol.add(
                        element(
                                GADetailsRespondentSol.builder()
                                        .generalApplicationType(generalApplicationsDetails
                                                .get().getValue().getGeneralApplicationType())
                                        .generalAppSubmittedDateGAspec(generalApplicationsDetails
                                                .get().getValue()
                                                .getGeneralAppSubmittedDateGAspec())
                                        .caseLink(CaseLink.builder().caseReference(String.valueOf(
                                                generalAppCaseData.getCcdCaseReference())).build())
                                        .caseState(newState)
                                        .parentClaimantIsApplicant(generalApplicationsDetails.get()
                                                                       .getValue().getParentClaimantIsApplicant()).build()));
            } else {
                /*
                * Update the ga with new state in respondent one solicitor collection
                * */
                log.info("Updating existing application state in Respondent One Solicitor collection to: {}", newState);
                gaDetailsRespondentSol = updateGaDetailsRespondentOne(caseData, newState, applicationId);
            }

            /*
             * Respondent Two Solicitor collection
             * */
            List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolTwo = ofNullable(
                caseData.getRespondentSolTwoGaAppDetails()).orElse(newArrayList());

            boolean isGaDetailsRespondentSolTwoPresent = gaDetailsRespondentSolTwo.stream()
                .anyMatch(gaRespondentTwoApp -> gaRespSolAppFilterCriteria(gaRespondentTwoApp, applicationId));

            if (!isGaDetailsRespondentSolTwoPresent) {
                gaDetailsRespondentSolTwo.add(
                    element(
                        GADetailsRespondentSol.builder()
                            .generalApplicationType(generalApplicationsDetails
                                                        .get().getValue().getGeneralApplicationType())
                            .generalAppSubmittedDateGAspec(generalApplicationsDetails
                                                               .get().getValue()
                                                               .getGeneralAppSubmittedDateGAspec())
                            .caseLink(CaseLink.builder().caseReference(String.valueOf(
                                generalAppCaseData.getCcdCaseReference())).build())
                            .caseState(newState)
                            .parentClaimantIsApplicant(generalApplicationsDetails
                                                           .get().getValue().getParentClaimantIsApplicant()).build()));
            } else {
                /*
                 * Update the ga with new state in respondent one solicitor collection
                 * */
                gaDetailsRespondentSolTwo = updateGaDetailsRespondentTwo(caseData, newState, applicationId);
            }

            /*
             * Claimant Solicitor collection
             * */
            List<Element<GeneralApplicationsDetails>> gaDetailsClaimant = ofNullable(
                caseData.getClaimantGaAppDetails()).orElse(newArrayList());

            boolean isGaDetailsClaimantPresent = gaDetailsClaimant.stream()
                .anyMatch(gaClaimant -> applicationFilterCriteria(gaClaimant, applicationId));

            if (!isGaDetailsClaimantPresent) {
                gaDetailsClaimant.add(
                    element(
                        GeneralApplicationsDetails.builder()
                            .generalApplicationType(generalApplicationsDetails
                                                        .get().getValue().getGeneralApplicationType())
                            .generalAppSubmittedDateGAspec(generalApplicationsDetails
                                                               .get().getValue()
                                                               .getGeneralAppSubmittedDateGAspec())
                            .caseLink(CaseLink.builder().caseReference(String.valueOf(
                                generalAppCaseData.getCcdCaseReference())).build())
                            .caseState(newState)
                            .parentClaimantIsApplicant(generalApplicationsDetails.get().getValue().getParentClaimantIsApplicant())
                            .build()));
            } else {
                /*
                 * Update the ga with new state in respondent one solicitor collection
                 * */
                gaDetailsClaimant = updateGaApplicationState(
                    caseData,
                    newState,
                    applicationId,
                    null
                );
            }

            /*
            * Judge Collection
            * */
            log.info("Updating Judge collection with new state: {}", newState);
            List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection = updateJudgeGaApplicationState(
                caseData,
                newState,
                applicationId
            );
            Map<String, Object> updateMap = getUpdatedCaseData(caseData, caseData.getGeneralApplications(),
                                                               gaDetailsClaimant,
                                                               gaDetailsRespondentSol,
                                                               gaDetailsRespondentSolTwo,
                                                               gaDetailsMasterCollection);
            /*
             * update documents
             * */
            if (gaDetailsRespondentSolTwo.isEmpty()) {
                roles[2] = null;
            }
            updateCaseDocument(updateMap, caseData, generalAppCaseData, roles);
            CaseDataContent caseDataContent = coreCaseDataService.caseDataContentFromStartEventResponse(
                startEventResponse, updateMap);

            coreCaseDataService.submitUpdate(parentCaseId, caseDataContent);
            log.info("Submitted update for Parent Case ID: {}", parentCaseId);
        }

    }

    public void updateParentApplicationVisibilityWithNewState(CaseData caseData, String newState) {
        updateParentApplicationVisibilityWithNewState(toGaCaseData(caseData), newState);
    }

    protected void updateCaseDocument(Map<String, Object> updateMap,
                                    CaseData civilCaseData, CaseData generalAppCaseData, String[] roles) {
        for (String role : roles) {
            if (Objects.nonNull(role)) {
                updateCaseDocumentByRole(updateMap, role,
                        civilCaseData, generalAppCaseData);
            }
        }
    }

    protected void updateCaseDocumentByRole(Map<String, Object> updateMap, String role,
                                          CaseData civilCaseData, CaseData generalAppCaseData) {
        for (String type : DOCUMENT_TYPES) {
            try {
                updateCaseDocumentByType(updateMap, type, role, civilCaseData, generalAppCaseData);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * Update GA document collection at civil case.
     *
     * @param updateMap      output map for update civil case.
     * @param civilCaseData civil case data.
     * @param generalAppCaseData    GA case data.
     * @param type document type.
     *             when get from GA data, add 'get' to the name then call getter to access related GA document field.
     *             when get from Civil data, add 'get' with role name then call getter
     * @param role role name. to be added with type to make the ga getter
     *
     */
    @SuppressWarnings("unchecked")
    protected void updateCaseDocumentByType(Map<String, Object> updateMap, String type, String role,
                                    CaseData civilCaseData, CaseData generalAppCaseData) throws Exception {
        if (Objects.isNull(role)) {
            return;
        }
        String gaCollectionName = type + "Document";
        if (type.equals(GA_EVIDENCE)) {
            gaCollectionName = CIVIL_GA_EVIDENCE + "Document";
        }
        if (type.equals(GA_ADDL)) {
            gaCollectionName = type + "Doc";
        }

        String civilCollectionName = type + "Doc" + role;
        Method gaGetter = ReflectionUtils.findMethod(CaseData.class,
                "get" + StringUtils.capitalize(gaCollectionName));
        List<Element<?>> gaDocs =
                (List<Element<?>>) (gaGetter != null ? gaGetter.invoke(generalAppCaseData) : null);
        Method civilGetter = ReflectionUtils.findMethod(CaseData.class,
                "get" + StringUtils.capitalize(civilCollectionName));
        List<Element<?>> civilDocs =
                (List<Element<?>>) ofNullable(civilGetter != null ? civilGetter.invoke(civilCaseData) : null)
                        .orElse(newArrayList());
        if (gaDocs != null && !(type.equals(GA_DRAFT_FORM))) {
            List<UUID> ids = civilDocs.stream().map(Element::getId).toList();
            for (Element<?> gaDoc : gaDocs) {
                if (!ids.contains(gaDoc.getId())) {
                    civilDocs.add(gaDoc);
                }
            }
        } else if (gaDocs != null && gaDocs.size() == 1
            && checkIfDocumentExists(civilDocs, gaDocs) < 1) {
            civilDocs.addAll(gaDocs);
        }
        updateMap.put(civilCollectionName, civilDocs.isEmpty() ? null : civilDocs);
    }

    @SuppressWarnings("unchecked")
    protected int checkIfDocumentExists(List<Element<?>> civilCaseDocumentList,
                                        List<Element<?>> gaCaseDocumentlist) {

        if (gaCaseDocumentlist.get(0).getValue().getClass().equals(CaseDocument.class)) {
            List<Element<CaseDocument>> civilCaseList = civilCaseDocumentList.stream()
                .map(element -> (Element<CaseDocument>) element)
                .toList();
            List<Element<CaseDocument>> gaCaseList = gaCaseDocumentlist.stream()
                .map(element -> (Element<CaseDocument>) element)
                .toList();

            return civilCaseList.stream().filter(civilDocument -> gaCaseList
               .parallelStream().anyMatch(gaDocument -> gaDocument.getValue().getDocumentLink().getDocumentUrl()
                   .equals(civilDocument.getValue().getDocumentLink().getDocumentUrl()))).toList().size();
        } else {
            List<Element<Document>> civilCaseList = civilCaseDocumentList.stream()
                   .map(element -> (Element<Document>) element)
                   .toList();

            List<Element<Document>> gaCaseList = gaCaseDocumentlist.stream()
                   .map(element -> (Element<Document>) element)
                   .toList();

            return civilCaseList.stream().filter(civilDocument -> gaCaseList
                   .parallelStream().anyMatch(gaDocument -> gaDocument.getValue().getDocumentUrl()
                       .equals(civilDocument.getValue().getDocumentUrl()))).toList().size();
        }
    }

    private List<Element<GeneralApplicationsDetails>> updateGaApplicationState(CaseData caseData, String newState,
                                                                               String applicationId, String[] roles) {
        log.info("Starting updateGaApplicationState. New state: {}, Application ID: {}", newState, applicationId);
        List<Element<GeneralApplicationsDetails>> generalApplications = ofNullable(
            caseData.getClaimantGaAppDetails()).orElse(newArrayList());

        if (!isEmpty(generalApplications)
            && generalApplications.stream()
                .anyMatch(applicant -> applicationFilterCriteria(applicant, applicationId))) {

            generalApplications.stream()
                    .filter(application -> applicationFilterCriteria(application, applicationId))
                    .findAny()
                    .orElseThrow(IllegalArgumentException::new)
                    .getValue().setCaseState(newState);
            if (Objects.nonNull(roles)) {
                roles[2] = CLAIMANT_ROLE;
            }
        }
        log.info("Completed updateJudgeGaApplicationState for caseId {}", caseData.getCcdCaseReference());
        return generalApplications;
    }

    private List<Element<GeneralApplicationsDetails>> updateJudgeGaApplicationState(CaseData caseData, String newState,
                                                                               String applicationId) {
        log.info("Starting updateJudgeGaApplicationState. New state: {}, Application ID: {}", newState, applicationId);
        List<Element<GeneralApplicationsDetails>> generalApplications = caseData.getGaDetailsMasterCollection();
        if (!isEmpty(generalApplications)
            && generalApplications.stream()
                .anyMatch(applicant -> applicationFilterCriteria(applicant, applicationId))) {
            log.info("Application with ID: {} found in the master collection for update.", applicationId);

            generalApplications.stream()
                    .filter(application -> applicationFilterCriteria(application, applicationId))
                    .findAny()
                    .orElseThrow(IllegalArgumentException::new)
                    .getValue().setCaseState(newState);
        }
        log.info("Completed updateJudgeGaApplicationState for caseId {}", caseData.getCcdCaseReference());
        return generalApplications;
    }

    private GeneralApplication buildGeneralApplication(GeneralApplication generalApplication) {
        GeneralApplication.GeneralApplicationBuilder applicationBuilder = generalApplication.toBuilder();

        applicationBuilder.generalAppRespondentAgreement(GARespondentOrderAgreement.builder().build())
            .generalAppPBADetails(GAPbaDetails.builder().build())
            .generalAppDetailsOfOrder(EMPTY)
            .generalAppReasonsOfOrder(EMPTY)
            .generalAppInformOtherParty(GAInformOtherParty.builder().build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
            .generalAppHearingDate(GAHearingDateGAspec.builder().build())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().build())
            .generalAppHearingDetails(GAHearingDetails.builder().build())
            .gaApplicantDisplayName(EMPTY)
            .civilServiceUserRoles(IdamUserDetails.builder().build())
            .generalAppRespondentSolicitors(Collections.emptyList())
            .generalAppEvidenceDocument(Collections.emptyList())
            .applicantPartyName(EMPTY)
            .claimant1PartyName(EMPTY)
            .claimant2PartyName(EMPTY)
            .defendant1PartyName(EMPTY)
            .isMultiParty(null)
            .isCcmccLocation(null)
            .caseAccessCategory(null)
            .defendant2PartyName(EMPTY)
            .generalAppSuperClaimType(EMPTY)
            .caseManagementCategory(GACaseManagementCategory.builder().build())
            .locationName(EMPTY)
            .generalAppHearingDate(GAHearingDateGAspec.builder().build())
            .certOfSC(CertOfSC.builder().build())
            .applicantPartyName(EMPTY).build();

        return applicationBuilder.build();
    }

    private List<Element<GeneralApplication>> addApplication(
        Optional<Element<GeneralApplication>> newApplicationElement, GeneralApplication application,
                                                             List<Element<GeneralApplication>>
                                                                 generalApplicationsList) {
        List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.addAll(generalApplicationsList);
        Element<GeneralApplication> elementToAdd;
        if (newApplicationElement.isPresent()) {
            elementToAdd = Element.<GeneralApplication>builder()
                .id(newApplicationElement.get().getId())
                .value(application)
                .build();
        } else {
            elementToAdd = element(application);
        }
        newApplication.add(elementToAdd);

        return newApplication;
    }

    private List<Element<GADetailsRespondentSol>> updateGaDetailsRespondentOne(CaseData caseData, String newState,
                                                                               String applicationId) {
        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol = ofNullable(
            caseData.getRespondentSolGaAppDetails()).orElse(newArrayList());
        log.info("Starting update for RespondentSolGaAppDetails with applicationId: {}", applicationId);
        if (!isEmpty(gaDetailsRespondentSol)
            && gaDetailsRespondentSol.stream()
                .anyMatch(respondentOne -> gaRespSolAppFilterCriteria(respondentOne, applicationId))) {
            log.info("Matching entry found for applicationId: {}. Updating case state to: {}", applicationId, newState);

            gaDetailsRespondentSol.stream()
                    .filter(respondentOne -> gaRespSolAppFilterCriteria(respondentOne, applicationId))
                    .findAny()
                    .orElseThrow(IllegalArgumentException::new)
                    .getValue().setCaseState(newState);
            log.info("Case state updated to {} for RespondentSol with applicationId: {}", newState, applicationId);

        }
        return gaDetailsRespondentSol;
    }

    private List<Element<GADetailsRespondentSol>> updateGaDetailsRespondentTwo(CaseData caseData, String newState,
                                                                               String applicationId) {
        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolTwo = ofNullable(
            caseData.getRespondentSolTwoGaAppDetails()).orElse(newArrayList());
        log.info("Starting update for RespondentSolTwoGaAppDetails with applicationId: {}", applicationId);

        if (!isEmpty(gaDetailsRespondentSolTwo)
            && gaDetailsRespondentSolTwo.stream()
                .anyMatch(respondentTwo -> gaRespSolAppFilterCriteria(respondentTwo, applicationId))) {
            log.info("Matching entry found for applicationId: {}. Updating case state to: {}", applicationId, newState);

            gaDetailsRespondentSolTwo.stream()
                    .filter(respondentTwo -> gaRespSolAppFilterCriteria(respondentTwo, applicationId))
                    .findAny()
                    .orElseThrow(IllegalArgumentException::new)
                    .getValue().setCaseState(newState);
            log.info("Case state updated to {} for RespondentSolTwo with applicationId: {}", newState, applicationId);

        }
        return gaDetailsRespondentSolTwo;
    }

    private boolean applicationFilterCriteria(Element<GeneralApplicationsDetails> gaDetails, String applicationId) {
        return gaDetails.getValue() != null
            && gaDetails.getValue().getCaseLink() != null
            && applicationId.equals(gaDetails.getValue().getCaseLink().getCaseReference());
    }

    private boolean applicationRespFilterCriteria(Element<GADetailsRespondentSol> gaDetails, String applicationId) {
        return gaDetails.getValue() != null
            && gaDetails.getValue().getCaseLink() != null
            && applicationId.equals(gaDetails.getValue().getCaseLink().getCaseReference());
    }

    private boolean gaRespSolAppFilterCriteria(Element<GADetailsRespondentSol> gaDetails, String applicationId) {

        return gaDetails.getValue() != null
            && gaDetails.getValue().getCaseLink() != null
            && applicationId.equals(gaDetails.getValue().getCaseLink().getCaseReference());
    }

    private Map<String, Object> getUpdatedCaseData(CaseData caseData,
                                                   List<Element<GeneralApplication>> civilGeneralApplications,
                                                   List<Element<GeneralApplicationsDetails>> claimantGaAppDetails,
                                                   List<Element<GADetailsRespondentSol>> respondentSolGaAppDetails,
                                                   List<Element<GADetailsRespondentSol>>
                                                       respondentSolTwoGaAppDetails,
                                                   List<Element<GeneralApplicationsDetails>>
                                                       gaDetailsMasterCollection) {
        Map<String, Object> output = caseData.toMap(mapper);
        output.put("generalApplications", civilGeneralApplications);
        output.put(GENERAL_APPLICATIONS_DETAILS_FOR_CLAIMANT, claimantGaAppDetails);
        output.put(GENERAL_APPLICATIONS_DETAILS_FOR_RESP_SOL, respondentSolGaAppDetails);
        output.put(GENERAL_APPLICATIONS_DETAILS_FOR_RESP_SOL_TWO, respondentSolTwoGaAppDetails);
        output.put(GENERAL_APPLICATIONS_DETAILS_FOR_JUDGE, gaDetailsMasterCollection);
        return output;
    }

    private Map<String, Object> getUpdateCaseDataForCollection(CaseData caseData,
                                                               List<Element<GeneralApplicationsDetails>>
                                                                   gaDetailsTranslationCollection) {
        Map<String, Object> output = caseData.toMap(mapper);
        output.put(GENERAL_APPLICATIONS_DETAILS_FOR_WELSH, gaDetailsTranslationCollection);
        return output;
    }

    private CaseData toCaseData(GeneralApplicationCaseData gaCaseData) {
        if (gaCaseData == null) {
            return null;
        }
        ObjectMapper mapperWithJsr310 = mapper.copy();
        mapperWithJsr310.registerModule(new JavaTimeModule());
        CaseData converted = mapperWithJsr310.convertValue(gaCaseData, CaseData.class);
        CaseData enriched = gaCaseDataEnricher.enrich(converted, gaCaseData);
        CaseData.CaseDataBuilder<?, ?> builder = enriched.toBuilder();
        if (gaCaseData.getCcdCaseReference() != null) {
            builder.ccdCaseReference(gaCaseData.getCcdCaseReference());
        }
        if (gaCaseData.getCcdState() != null) {
            builder.ccdState(gaCaseData.getCcdState());
        }
        return builder.build();
    }

    private GeneralApplicationCaseData toGaCaseData(CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        GeneralApplicationCaseData converted = mapper.convertValue(caseData, GeneralApplicationCaseData.class);
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder builder = converted.toBuilder();
        Long gaReference = caseData.getCcdCaseReference();
        if (gaReference == null && !isEmpty(caseData.getGeneralApplications())) {
            gaReference = caseData.getGeneralApplications().stream()
                .map(Element::getValue)
                .map(GeneralApplication::getCaseLink)
                .filter(Objects::nonNull)
                .map(CaseLink::getCaseReference)
                .filter(StringUtils::hasText)
                .map(Long::valueOf)
                .findFirst()
                .orElse(null);
        }
        if (gaReference != null) {
            builder.ccdCaseReference(gaReference);
        }
        if (caseData.getCcdState() != null) {
            builder.ccdState(caseData.getCcdState());
        }
        return builder.build();
    }

}
