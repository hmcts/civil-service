package uk.gov.hmcts.reform.civil.ga.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

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
    private final GaCoreCaseDataService coreCaseDataService;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper mapper;

    private static final Logger log = LoggerFactory.getLogger(ParentCaseUpdateHelper.class);

    private static final String GENERAL_APPLICATIONS_DETAILS_FOR_CLAIMANT = "claimantGaAppDetails";
    private static final String GENERAL_APPLICATIONS_DETAILS_FOR_RESP_SOL = "respondentSolGaAppDetails";
    private static final String GENERAL_APPLICATIONS_DETAILS_FOR_RESP_SOL_TWO = "respondentSolTwoGaAppDetails";
    private static final String GENERAL_APPLICATIONS_DETAILS_FOR_JUDGE = "gaDetailsMasterCollection";
    private static final String GENERAL_APPLICATIONS_DETAILS_FOR_WELSH = "gaDetailsTranslationCollection";
    private static final String GA_DRAFT_FORM = "gaDraft";
    private static final String GA_ADDL = "gaAddl";
    private static final String[] DOCUMENT_TYPES = {
        "generalOrder", "dismissalOrder",
        "directionOrder", "hearingNotice",
        "gaResp", GA_DRAFT_FORM, GA_ADDL
    };
    private static final String CLAIMANT_ROLE = "Claimant";
    private static final String RESPONDENT_SOL_ROLE = "RespondentSol";
    private static final String RESPONDENT_SOL_TWO_ROLE = "RespondentSolTwo";
    private static final String GA_EVIDENCE = "gaEvidence";
    private static final String CIVIL_GA_EVIDENCE = "generalAppEvidence";

    protected static List<CaseState> DOCUMENT_STATES = Arrays.asList(
            AWAITING_ADDITIONAL_INFORMATION,
            AWAITING_WRITTEN_REPRESENTATIONS,
            AWAITING_DIRECTIONS_ORDER_DOCS,
            PENDING_APPLICATION_ISSUED,
            APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION
    );

    private final String[] roles = {CLAIMANT_ROLE, RESPONDENT_SOL_ROLE, RESPONDENT_SOL_TWO_ROLE};

    public void updateParentWithGAState(GeneralApplicationCaseData generalAppCaseData, String newState) {

        String applicationId = generalAppCaseData.getCcdCaseReference().toString();
        String parentCaseId = generalAppCaseData.getGeneralAppParentCaseLink().getCaseReference();
        String[] docVisibilityRoles = new String[4];

        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(parentCaseId,
                                                                                UPDATE_CASE_WITH_GA_STATE);
        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails());

        ParentCollections parentCollections = updateParentCollectionsForState(
            caseData,
            newState,
            applicationId,
            docVisibilityRoles
        );
        docVisibilityRoles[3] = "Staff";

        List<Element<GeneralApplication>> civilGeneralApplications = getUpdatedCivilGeneralApplications(
            caseData,
            generalAppCaseData,
            applicationId,
            parentCaseId
        );

        Map<String, Object> updateMap = getUpdatedCaseData(
            caseData,
            civilGeneralApplications,
            parentCollections.claimantDetails(),
            parentCollections.respondentDetails(),
            parentCollections.respondentTwoDetails(),
            parentCollections.judgeDetails()
        );
        updateDocumentsAndEvidence(updateMap, caseData, generalAppCaseData, docVisibilityRoles);
        log.info("updateParentWithGAState() Civil General Applications about to submit for case ID: {}", parentCaseId);
        submitParentCaseUpdate(parentCaseId, startEventResponse, updateMap);
    }

    protected void updateEvidence(Map<String, Object> updateMap, GeneralApplicationCaseData civilCaseData,
                                  GeneralApplicationCaseData generalAppCaseData, String[] docVisibilityRoles) {
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
                                         GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData generalAppCaseData) {
        for (String role : roles) {
            try {
                updateCaseDocumentByType(updateMap, type, role, civilCaseData, generalAppCaseData);
            } catch (ReflectiveOperationException e) {
                log.error(e.getMessage());
            }
        }
    }

    protected String findGaCreator(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData generalAppCaseData) {
        log.info("Starting findGaCreator. Evaluating GA creator for Application ID: {}", generalAppCaseData.getCcdCaseReference());
        if (isClaimantCreator(generalAppCaseData)) {
            return CLAIMANT_ROLE;
        }
        if (isApplicantLipCreator(generalAppCaseData)) {
            return RESPONDENT_SOL_ROLE;
        }
        String creatorId = generalAppCaseData.getGeneralAppApplnSolicitor().getOrganisationIdentifier();
        if (isRespondentOneCreator(civilCaseData, creatorId)) {
            log.info("GA creator is Respondent Solicitor 1.");
            return RESPONDENT_SOL_ROLE;
        }
        if (isRespondentTwoCreator(civilCaseData, generalAppCaseData, creatorId)) {
            log.info("GA creator is Respondent Solicitor 2.");
            return RESPONDENT_SOL_TWO_ROLE;
        }
        return null;
    }

    private boolean isClaimantCreator(GeneralApplicationCaseData generalAppCaseData) {
        return generalAppCaseData.getParentClaimantIsApplicant().equals(YES);
    }

    private boolean isApplicantLipCreator(GeneralApplicationCaseData generalAppCaseData) {
        return generalAppCaseData.getIsGaApplicantLip() == YES;
    }

    private boolean isRespondentOneCreator(GeneralApplicationCaseData civilCaseData, String creatorId) {
        String respondent1OrganisationId = getOrganisationId(
            civilCaseData.getRespondent1OrganisationPolicy(),
            civilCaseData.getRespondent1OrganisationIDCopy()
        );
        log.debug("GA creator Organisation ID: {}, Respondent 1 Organisation ID: {}", creatorId, respondent1OrganisationId);
        return creatorId.equals(respondent1OrganisationId);
    }

    private boolean isRespondentTwoCreator(GeneralApplicationCaseData civilCaseData,
                                           GeneralApplicationCaseData generalAppCaseData,
                                           String creatorId) {
        if (!canRespondentTwoBeCreator(civilCaseData, generalAppCaseData)) {
            return false;
        }
        String respondent2OrganisationId = getOrganisationId(
            civilCaseData.getRespondent2OrganisationPolicy(),
            civilCaseData.getRespondent2OrganisationIDCopy()
        );
        return creatorId.equals(respondent2OrganisationId);
    }

    private boolean canRespondentTwoBeCreator(GeneralApplicationCaseData civilCaseData,
                                              GeneralApplicationCaseData generalAppCaseData) {
        return generalAppCaseData.getIsMultiParty().equals(YES)
            && civilCaseData.getAddApplicant2().equals(NO)
            && civilCaseData.getRespondent2SameLegalRepresentative().equals(NO);
    }

    private String getOrganisationId(OrganisationPolicy organisationPolicy, String fallbackOrganisationId) {
        return organisationPolicy.getOrganisation() != null
            ? organisationPolicy.getOrganisation().getOrganisationID()
            : fallbackOrganisationId;
    }

    public void updateMasterCollectionForHwf(GeneralApplicationCaseData generalAppCaseData) {

        String parentCaseId = generalAppCaseData.getGeneralAppParentCaseLink().getCaseReference();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
            parentCaseId,
            UPDATE_CASE_WITH_GA_STATE
        );

        GeneralApplicationCaseData parentCaseData = caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails());
        String applicationId = generalAppCaseData.getCcdCaseReference().toString();

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
                            new GeneralApplicationsDetails()
                                .setGeneralApplicationType(generalApplicationsDetailsElement.getValue().getGeneralApplicationType())
                                .setGeneralAppSubmittedDateGAspec(generalApplicationsDetailsElement.getValue()
                                                                      .getGeneralAppSubmittedDateGAspec())
                                .setCaseLink(new CaseLink(String.valueOf(generalAppCaseData.getCcdCaseReference())))));
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

    public void updateJudgeAndRespondentCollectionAfterPayment(GeneralApplicationCaseData generalAppCaseData) {

        String applicationId = generalAppCaseData.getCcdCaseReference().toString();
        String parentCaseId = generalAppCaseData.getGeneralAppParentCaseLink().getCaseReference();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
            parentCaseId,
            UPDATE_CASE_WITH_GA_STATE
        );
        GeneralApplicationCaseData parentCaseData = caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails());

        ParentCollections parentCollections = getParentCollections(parentCaseData);
        updateMasterCollectionsAfterPayment(parentCaseData, generalAppCaseData, applicationId, parentCollections);
        if (shouldShareApplicationAfterPayment(generalAppCaseData)) {
            updateVisibleCollectionsAfterPayment(parentCaseData, generalAppCaseData, applicationId, parentCollections);
        }

        Map<String, Object> updateMap = getUpdatedCaseData(parentCaseData, parentCaseData.getGeneralApplications(),
                                                           parentCollections.claimantDetails(),
                                                           parentCollections.respondentDetails(),
                                                           parentCollections.respondentTwoDetails(),
                                                           parentCollections.judgeDetails());
        removeApplicationFromTranslationCollection(parentCaseData, updateMap, applicationId);
        submitParentCaseUpdate(parentCaseId, startEventResponse, updateMap);
    }

    public void updateCollectionForWelshApplication(GeneralApplicationCaseData generalAppCaseData) {
        String applicationId = generalAppCaseData.getCcdCaseReference().toString();
        String parentCaseId = generalAppCaseData.getGeneralAppParentCaseLink().getCaseReference();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
            parentCaseId,
            UPDATE_CASE_WITH_GA_STATE
        );
        GeneralApplicationCaseData parentCaseData = caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails());
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

    private void addClaimantApplicationDetails(GeneralApplicationCaseData generalAppCaseData, String applicationId,
                                               List<Element<GeneralApplicationsDetails>> gaTranslationDetails,
                                               List<Element<GeneralApplicationsDetails>> gaClaimantDetails) {
        Optional<Element<GeneralApplicationsDetails>> claimantCollection = gaClaimantDetails
            .stream().filter(claimantApp -> applicationFilterCriteria(claimantApp, applicationId)).findAny();
        claimantCollection.ifPresent(generalApplicationsDetailsElement -> gaTranslationDetails.add(
            element(
                createGeneralApplicationsDetails(
                    generalApplicationsDetailsElement.getValue(),
                    generalAppCaseData.getCcdCaseReference()
                ))));
    }

    private void removeApplicationFromTranslationCollection(GeneralApplicationCaseData parentCaseData, Map<String, Object> updateMap,
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

    private void updateRespCollectionForMultiParty(GeneralApplicationCaseData generalAppCaseData, String applicationId,
                                                  List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol,
                                                  List<Element<GADetailsRespondentSol>> gaRespondentSol) {
        Optional<Element<GADetailsRespondentSol>> respCollection = gaRespondentSol
            .stream().filter(respCollectionApp -> applicationRespFilterCriteria(respCollectionApp, applicationId)).findAny();

        Optional<Element<GADetailsRespondentSol>> gaToBeAdded = gaDetailsRespondentSol
            .stream().filter(respCollectionElement -> gaRespSolAppFilterCriteria(respCollectionElement, applicationId)).findAny();

        /*
          To Prevent duplicate, Check if the application already present in the Respondent Collection before adding it from another Collection
         */
        if (gaToBeAdded.isEmpty()) {
            respCollection.ifPresent(generalApplicationsDetailsElement -> gaDetailsRespondentSol.add(
                element(
                    createRespondentSolDetails(
                        generalApplicationsDetailsElement.getValue(),
                        generalAppCaseData.getCcdCaseReference()
                    ))));
        }
    }

    private void updateRespCollectionFromClaimant(GeneralApplicationCaseData generalAppCaseData, String applicationId,
                                                  List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol,
                                                  List<Element<GeneralApplicationsDetails>> gaClaimantDetails) {

        Optional<Element<GeneralApplicationsDetails>> claimantCollection = gaClaimantDetails
            .stream().filter(claimantApp -> applicationFilterCriteria(claimantApp, applicationId)).findAny();
        claimantCollection.ifPresent(generalApplicationsDetailsElement -> gaDetailsRespondentSol.add(
            element(
                createRespondentSolDetails(
                    generalApplicationsDetailsElement.getValue(),
                    generalAppCaseData.getCcdCaseReference()
                ))));
    }

    private void updateJudgeOrClaimantFromRespCollection(GeneralApplicationCaseData generalAppCaseData, String applicationId,
                                                         List<Element<GeneralApplicationsDetails>> gaMasterDetails,
                                                         List<Element<GADetailsRespondentSol>> gaDetailsRespondentSol) {
        if (!gaDetailsRespondentSol.isEmpty()) {

            Optional<Element<GADetailsRespondentSol>> respondentSolCollection = gaDetailsRespondentSol
                .stream().filter(respondentSolElement2 -> gaRespSolAppFilterCriteria(respondentSolElement2, applicationId)).findAny();

            Optional<Element<GeneralApplicationsDetails>> masterCollection = gaMasterDetails
                .stream().filter(masterCollectionElement -> applicationFilterCriteria(masterCollectionElement, applicationId)).findAny();

            /*
              To Prevent duplicate, Check if the application already present in the Master Collection before adding it from Respondent Collection
             */
            if (masterCollection.isEmpty()) {
                log.info("Application with Case ID {} is added to the target collection", generalAppCaseData.getCcdCaseReference());
                respondentSolCollection.ifPresent(respondentSolElement -> gaMasterDetails.add(
                    element(
                        createGeneralApplicationsDetails(
                            respondentSolElement.getValue(),
                            generalAppCaseData.getCcdCaseReference()
                        ))));
            }
        }
    }

    public void updateParentApplicationVisibilityWithNewState(GeneralApplicationCaseData generalAppCaseData, String newState) {

        String applicationId = generalAppCaseData.getCcdCaseReference().toString();
        String parentCaseId = generalAppCaseData.getGeneralAppParentCaseLink().getCaseReference();
        log.info("Starting updateParentApplicationVisibilityWithNewState. New state: {}, Application ID: {}, Parent Case ID: {}", newState, applicationId, parentCaseId);

        StartEventResponse startEventResponse = coreCaseDataService
            .startUpdate(parentCaseId, UPDATE_CASE_WITH_GA_STATE);

        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails());

        Optional<Element<GeneralApplicationsDetails>> generalApplicationsDetails = findMasterApplication(caseData, applicationId);
        if (generalApplicationsDetails.isEmpty()) {
            return;
        }

        log.info("Found application in master collection with ID: {}", applicationId);
        ParentCollections parentCollections = updateVisibleCollectionsForState(
            caseData,
            newState,
            applicationId,
            generalApplicationsDetails.get()
        );
        Map<String, Object> updateMap = getUpdatedCaseData(caseData, caseData.getGeneralApplications(),
                                                           parentCollections.claimantDetails(),
                                                           parentCollections.respondentDetails(),
                                                           parentCollections.respondentTwoDetails(),
                                                           parentCollections.judgeDetails());
        if (parentCollections.respondentTwoDetails().isEmpty()) {
            roles[2] = null;
        }
        updateCaseDocument(updateMap, caseData, generalAppCaseData, roles);
        submitParentCaseUpdate(parentCaseId, startEventResponse, updateMap);
        log.info("Submitted update for Parent Case ID: {}", parentCaseId);
    }

    private ParentCollections updateParentCollectionsForState(GeneralApplicationCaseData caseData,
                                                              String newState,
                                                              String applicationId,
                                                              String[] docVisibilityRoles) {
        List<Element<GADetailsRespondentSol>> respondentDetails = ofNullable(caseData.getRespondentSolGaAppDetails()).orElse(newArrayList());
        List<Element<GADetailsRespondentSol>> respondentTwoDetails = ofNullable(caseData.getRespondentSolTwoGaAppDetails()).orElse(newArrayList());
        updateRespondentCollectionState(respondentDetails, applicationId, newState, docVisibilityRoles, 0, RESPONDENT_SOL_ROLE);
        updateRespondentCollectionState(respondentTwoDetails, applicationId, newState, docVisibilityRoles, 1, RESPONDENT_SOL_TWO_ROLE);

        List<Element<GeneralApplicationsDetails>> claimantDetails = updateGaApplicationState(
            caseData,
            newState,
            applicationId,
            docVisibilityRoles
        );
        List<Element<GeneralApplicationsDetails>> judgeDetails = updateJudgeGaApplicationState(
            caseData,
            newState,
            applicationId
        );
        return new ParentCollections(claimantDetails, respondentDetails, respondentTwoDetails, judgeDetails);
    }

    private void updateRespondentCollectionState(List<Element<GADetailsRespondentSol>> respondentDetails,
                                                 String applicationId,
                                                 String newState,
                                                 String[] docVisibilityRoles,
                                                 int roleIndex,
                                                 String roleName) {
        if (isEmpty(respondentDetails)) {
            return;
        }
        Optional<Element<GADetailsRespondentSol>> matchingApplication = respondentDetails.stream()
            .filter(gaRespondentApp -> gaRespSolAppFilterCriteria(gaRespondentApp, applicationId))
            .findAny();
        matchingApplication.ifPresent(application -> {
            application.getValue().setCaseState(newState);
            docVisibilityRoles[roleIndex] = roleName;
        });
    }

    private List<Element<GeneralApplication>> getUpdatedCivilGeneralApplications(GeneralApplicationCaseData caseData,
                                                                                 GeneralApplicationCaseData generalAppCaseData,
                                                                                 String applicationId,
                                                                                 String parentCaseId) {
        List<Element<GeneralApplication>> civilGeneralApplications = caseData.getGeneralApplications();
        log.info("updateParentWithGAState() Civil General Applications before for case ID: {}", parentCaseId);
        if (!shouldRebuildPendingIssuedApplication(generalAppCaseData, civilGeneralApplications)) {
            return civilGeneralApplications;
        }
        return rebuildPendingIssuedApplication(civilGeneralApplications, applicationId, parentCaseId);
    }

    private boolean shouldRebuildPendingIssuedApplication(GeneralApplicationCaseData generalAppCaseData,
                                                          List<Element<GeneralApplication>> civilGeneralApplications) {
        return generalAppCaseData.getCcdState().equals(PENDING_APPLICATION_ISSUED) && !isEmpty(civilGeneralApplications);
    }

    private List<Element<GeneralApplication>> rebuildPendingIssuedApplication(List<Element<GeneralApplication>> civilGeneralApplications,
                                                                              String applicationId,
                                                                              String parentCaseId) {
        List<Element<GeneralApplication>> generalApplicationsList = civilGeneralApplications.stream()
            .filter(app -> !isCurrentApplication(app, applicationId))
            .toList();
        Optional<Element<GeneralApplication>> currentApplicationElement = civilGeneralApplications.stream()
            .filter(app -> isCurrentApplication(app, applicationId))
            .findFirst();
        GeneralApplication currentApplication = currentApplicationElement
            .map(Element::getValue)
            .orElseThrow(() -> new IllegalArgumentException("Civil general application not found for parent case id: " + parentCaseId));
        return addApplication(currentApplicationElement, buildGeneralApplication(currentApplication), generalApplicationsList);
    }

    private boolean isCurrentApplication(Element<GeneralApplication> application, String applicationId) {
        return application.getValue().getCaseLink() != null
            && application.getValue().getCaseLink().getCaseReference().equals(applicationId);
    }

    private void updateDocumentsAndEvidence(Map<String, Object> updateMap,
                                            GeneralApplicationCaseData caseData,
                                            GeneralApplicationCaseData generalAppCaseData,
                                            String[] docVisibilityRoles) {
        if (DOCUMENT_STATES.contains(generalAppCaseData.getCcdState())) {
            updateCaseDocument(updateMap, caseData, generalAppCaseData, docVisibilityRoles);
        }
        if (hasEvidenceOrDraftDocuments(generalAppCaseData)) {
            updateEvidence(updateMap, caseData, generalAppCaseData, docVisibilityRoles);
        }
    }

    private boolean hasEvidenceOrDraftDocuments(GeneralApplicationCaseData generalAppCaseData) {
        return (Objects.nonNull(generalAppCaseData.getGeneralAppEvidenceDocument())
            && !generalAppCaseData.getGeneralAppEvidenceDocument().isEmpty())
            || (Objects.nonNull(generalAppCaseData.getGaDraftDocument())
            && !generalAppCaseData.getGaDraftDocument().isEmpty());
    }

    private void submitParentCaseUpdate(String parentCaseId, StartEventResponse startEventResponse, Map<String, Object> updateMap) {
        CaseDataContent caseDataContent = coreCaseDataService.caseDataContentFromStartEventResponse(startEventResponse, updateMap);
        coreCaseDataService.submitUpdate(parentCaseId, caseDataContent);
    }

    private ParentCollections getParentCollections(GeneralApplicationCaseData parentCaseData) {
        return new ParentCollections(
            ofNullable(parentCaseData.getClaimantGaAppDetails()).orElse(newArrayList()),
            ofNullable(parentCaseData.getRespondentSolGaAppDetails()).orElse(newArrayList()),
            ofNullable(parentCaseData.getRespondentSolTwoGaAppDetails()).orElse(newArrayList()),
            ofNullable(parentCaseData.getGaDetailsMasterCollection()).orElse(newArrayList())
        );
    }

    private void updateMasterCollectionsAfterPayment(GeneralApplicationCaseData parentCaseData,
                                                     GeneralApplicationCaseData generalAppCaseData,
                                                     String applicationId,
                                                     ParentCollections parentCollections) {
        if (generalAppCaseData.getParentClaimantIsApplicant().equals(YES)) {
            addClaimantApplicationDetails(
                generalAppCaseData,
                applicationId,
                parentCollections.judgeDetails(),
                parentCollections.claimantDetails()
            );
            return;
        }

        if (shouldCopyRespondentCollectionToMaster(parentCaseData, generalAppCaseData)) {
            updateJudgeOrClaimantFromRespCollection(
                generalAppCaseData,
                applicationId,
                parentCollections.judgeDetails(),
                parentCollections.respondentDetails()
            );
        }
        updateMultipartyMasterCollections(generalAppCaseData, applicationId, parentCollections);
    }

    private void updateMultipartyMasterCollections(GeneralApplicationCaseData generalAppCaseData,
                                                   String applicationId,
                                                   ParentCollections parentCollections) {
        if (generalAppCaseData.getIsMultiParty().equals(YES) && !parentCollections.respondentDetails().isEmpty()) {
            updateJudgeOrClaimantFromRespCollection(
                generalAppCaseData,
                applicationId,
                parentCollections.judgeDetails(),
                parentCollections.respondentDetails()
            );
        }
        if (generalAppCaseData.getIsMultiParty().equals(YES) && !parentCollections.respondentTwoDetails().isEmpty()) {
            updateJudgeOrClaimantFromRespCollection(
                generalAppCaseData,
                applicationId,
                parentCollections.judgeDetails(),
                parentCollections.respondentTwoDetails()
            );
        }
    }

    private boolean shouldCopyRespondentCollectionToMaster(GeneralApplicationCaseData parentCaseData,
                                                           GeneralApplicationCaseData generalAppCaseData) {
        return (Objects.nonNull(parentCaseData.getRespondent2SameLegalRepresentative())
            && parentCaseData.getRespondent2SameLegalRepresentative().equals(YES))
            || generalAppCaseData.getIsMultiParty().equals(NO);
    }

    private boolean shouldShareApplicationAfterPayment(GeneralApplicationCaseData generalAppCaseData) {
        return (generalAppCaseData.getGeneralAppRespondentAgreement().getHasAgreed().equals(NO)
            && ofNullable(generalAppCaseData.getGeneralAppInformOtherParty()).isPresent()
            && YES.equals(generalAppCaseData.getGeneralAppInformOtherParty().getIsWithNotice()))
            || generalAppCaseData.getGeneralAppRespondentAgreement().getHasAgreed().equals(YES);
    }

    private void updateVisibleCollectionsAfterPayment(GeneralApplicationCaseData parentCaseData,
                                                      GeneralApplicationCaseData generalAppCaseData,
                                                      String applicationId,
                                                      ParentCollections parentCollections) {
        if (generalAppCaseData.getParentClaimantIsApplicant().equals(YES)) {
            updateRespondentCollectionsFromClaimant(generalAppCaseData, applicationId, parentCollections);
        } else if (shouldCopyRespondentCollectionToMaster(parentCaseData, generalAppCaseData)) {
            updateJudgeOrClaimantFromRespCollection(
                generalAppCaseData,
                applicationId,
                parentCollections.claimantDetails(),
                parentCollections.respondentDetails()
            );
        }

        updateMultipartyVisibleCollections(generalAppCaseData, applicationId, parentCollections);
    }

    private void updateRespondentCollectionsFromClaimant(GeneralApplicationCaseData generalAppCaseData,
                                                         String applicationId,
                                                         ParentCollections parentCollections) {
        updateRespCollectionFromClaimant(
            generalAppCaseData,
            applicationId,
            parentCollections.respondentDetails(),
            parentCollections.claimantDetails()
        );
        if (generalAppCaseData.getIsMultiParty().equals(YES)) {
            updateRespCollectionFromClaimant(
                generalAppCaseData,
                applicationId,
                parentCollections.respondentTwoDetails(),
                parentCollections.claimantDetails()
            );
        }
    }

    private void updateMultipartyVisibleCollections(GeneralApplicationCaseData generalAppCaseData,
                                                    String applicationId,
                                                    ParentCollections parentCollections) {
        syncMultipartyVisibleCollections(
            generalAppCaseData,
            applicationId,
            parentCollections.respondentDetails(),
            parentCollections.respondentTwoDetails(),
            parentCollections.claimantDetails(),
            "Respondent One"
        );
        syncMultipartyVisibleCollections(
            generalAppCaseData,
            applicationId,
            parentCollections.respondentTwoDetails(),
            parentCollections.respondentDetails(),
            parentCollections.claimantDetails(),
            "Respondent Two"
        );
    }

    private void syncMultipartyVisibleCollections(GeneralApplicationCaseData generalAppCaseData,
                                                  String applicationId,
                                                  List<Element<GADetailsRespondentSol>> sourceDetails,
                                                  List<Element<GADetailsRespondentSol>> targetDetails,
                                                  List<Element<GeneralApplicationsDetails>> claimantDetails,
                                                  String initiatorLabel) {
        if (generalAppCaseData.getIsMultiParty().equals(YES) && !sourceDetails.isEmpty()) {
            log.info("Multiparty case and {} initiates the GA for Case ID: {}", initiatorLabel, generalAppCaseData.getCcdCaseReference());
            updateJudgeOrClaimantFromRespCollection(
                generalAppCaseData,
                applicationId,
                claimantDetails,
                sourceDetails
            );
            updateRespCollectionForMultiParty(
                generalAppCaseData,
                applicationId,
                targetDetails,
                sourceDetails
            );
        }
    }

    private Optional<Element<GeneralApplicationsDetails>> findMasterApplication(GeneralApplicationCaseData caseData, String applicationId) {
        return caseData.getGaDetailsMasterCollection()
            .stream()
            .filter(application -> applicationFilterCriteria(application, applicationId))
            .findAny();
    }

    private ParentCollections updateVisibleCollectionsForState(GeneralApplicationCaseData caseData,
                                                               String newState,
                                                               String applicationId,
                                                               Element<GeneralApplicationsDetails> masterApplication) {
        List<Element<GADetailsRespondentSol>> respondentDetails = upsertRespondentVisibilityCollection(
            ofNullable(caseData.getRespondentSolGaAppDetails()).orElse(newArrayList()),
            caseData,
            masterApplication,
            newState,
            applicationId,
            true
        );
        List<Element<GADetailsRespondentSol>> respondentTwoDetails = upsertRespondentVisibilityCollection(
            ofNullable(caseData.getRespondentSolTwoGaAppDetails()).orElse(newArrayList()),
            caseData,
            masterApplication,
            newState,
            applicationId,
            false
        );
        List<Element<GeneralApplicationsDetails>> claimantDetails = upsertClaimantVisibilityCollection(
            caseData,
            masterApplication,
            newState,
            applicationId
        );
        log.info("Updating Judge collection with new state: {}", newState);
        List<Element<GeneralApplicationsDetails>> judgeDetails = updateJudgeGaApplicationState(
            caseData,
            newState,
            applicationId
        );
        return new ParentCollections(claimantDetails, respondentDetails, respondentTwoDetails, judgeDetails);
    }

    private List<Element<GADetailsRespondentSol>> upsertRespondentVisibilityCollection(List<Element<GADetailsRespondentSol>> respondentDetails,
                                                                                       GeneralApplicationCaseData caseData,
                                                                                       Element<GeneralApplicationsDetails> masterApplication,
                                                                                       String newState,
                                                                                       String applicationId,
                                                                                       boolean isRespondentOne) {
        boolean isPresent = respondentDetails.stream().anyMatch(gaRespondentApp -> gaRespSolAppFilterCriteria(gaRespondentApp, applicationId));
        String collectionName = isRespondentOne ? "Respondent One Solicitor" : "Respondent Two Solicitor";
        if (!isPresent) {
            log.info("Adding application to {} collection.", collectionName);
            respondentDetails.add(element(createRespondentSolDetails(masterApplication, newState, caseData)));
            return respondentDetails;
        }

        log.info("Updating existing application state in {} collection to: {}", collectionName, newState);
        return updateGaDetailsRespondentCollection(respondentDetails, newState, applicationId, collectionName);
    }

    private GADetailsRespondentSol createRespondentSolDetails(Element<GeneralApplicationsDetails> masterApplication,
                                                              String newState,
                                                              GeneralApplicationCaseData generalAppCaseData) {
        return createRespondentSolDetails(masterApplication.getValue(), generalAppCaseData.getCcdCaseReference())
            .setCaseState(newState);
    }

    private GADetailsRespondentSol createRespondentSolDetails(GeneralApplicationsDetails source, Long caseReference) {
        return new GADetailsRespondentSol()
            .setGeneralApplicationType(source.getGeneralApplicationType())
            .setGeneralAppSubmittedDateGAspec(source.getGeneralAppSubmittedDateGAspec())
            .setCaseLink(new CaseLink(String.valueOf(caseReference)))
            .setParentClaimantIsApplicant(source.getParentClaimantIsApplicant());
    }

    private GADetailsRespondentSol createRespondentSolDetails(GADetailsRespondentSol source, Long caseReference) {
        return new GADetailsRespondentSol()
            .setGeneralApplicationType(source.getGeneralApplicationType())
            .setGeneralAppSubmittedDateGAspec(source.getGeneralAppSubmittedDateGAspec())
            .setCaseLink(new CaseLink(String.valueOf(caseReference)))
            .setParentClaimantIsApplicant(source.getParentClaimantIsApplicant());
    }

    private List<Element<GeneralApplicationsDetails>> upsertClaimantVisibilityCollection(GeneralApplicationCaseData caseData,
                                                                                         Element<GeneralApplicationsDetails> masterApplication,
                                                                                         String newState,
                                                                                         String applicationId) {
        List<Element<GeneralApplicationsDetails>> claimantDetails = ofNullable(caseData.getClaimantGaAppDetails()).orElse(newArrayList());
        boolean isPresent = claimantDetails.stream().anyMatch(gaClaimant -> applicationFilterCriteria(gaClaimant, applicationId));
        if (!isPresent) {
            claimantDetails.add(element(createClaimantDetails(masterApplication, newState, caseData)));
            return claimantDetails;
        }
        return updateGaApplicationState(caseData, newState, applicationId, null);
    }

    private GeneralApplicationsDetails createClaimantDetails(Element<GeneralApplicationsDetails> masterApplication,
                                                             String newState,
                                                             GeneralApplicationCaseData generalAppCaseData) {
        return createGeneralApplicationsDetails(masterApplication.getValue(), generalAppCaseData.getCcdCaseReference())
            .setCaseState(newState);
    }

    protected void updateCaseDocument(Map<String, Object> updateMap,
                                    GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData generalAppCaseData, String[] roles) {
        for (String role : roles) {
            if (Objects.nonNull(role)) {
                updateCaseDocumentByRole(updateMap, role,
                        civilCaseData, generalAppCaseData);
            }
        }
    }

    protected void updateCaseDocumentByRole(Map<String, Object> updateMap, String role,
                                          GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData generalAppCaseData) {
        for (String type : DOCUMENT_TYPES) {
            try {
                updateCaseDocumentByType(updateMap, type, role, civilCaseData, generalAppCaseData);
            } catch (ReflectiveOperationException e) {
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
    protected void updateCaseDocumentByType(Map<String, Object> updateMap, String type, String role,
                                    GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData generalAppCaseData) throws ReflectiveOperationException {
        if (Objects.isNull(role)) {
            return;
        }
        String gaCollectionName = getGaCollectionName(type);
        String civilCollectionName = type + "Doc" + role;
        List<Element<?>> gaDocs = getDocumentCollection(generalAppCaseData, gaCollectionName);
        List<Element<?>> civilDocs = getDocumentCollection(civilCaseData, civilCollectionName);
        mergeDocumentCollections(type, civilDocs, gaDocs);
        updateMap.put(civilCollectionName, civilDocs.isEmpty() ? null : civilDocs);
    }

    private String getGaCollectionName(String type) {
        if (type.equals(GA_EVIDENCE)) {
            return CIVIL_GA_EVIDENCE + "Document";
        }
        if (type.equals(GA_ADDL)) {
            return type + "Doc";
        }
        return type + "Document";
    }

    @SuppressWarnings("unchecked")
    private List<Element<?>> getDocumentCollection(GeneralApplicationCaseData caseData, String collectionName)
        throws ReflectiveOperationException {
        Method getter = ReflectionUtils.findMethod(
            GeneralApplicationCaseData.class,
            "get" + StringUtils.capitalize(collectionName)
        );
        return (List<Element<?>>) ofNullable(getter != null ? getter.invoke(caseData) : null).orElse(newArrayList());
    }

    private void mergeDocumentCollections(String type, List<Element<?>> civilDocs, List<Element<?>> gaDocs) {
        if (gaDocs.isEmpty()) {
            return;
        }
        if (type.equals(GA_DRAFT_FORM)) {
            addSingleDraftDocumentIfMissing(civilDocs, gaDocs);
            return;
        }
        addDocumentsByMissingId(civilDocs, gaDocs);
    }

    private void addSingleDraftDocumentIfMissing(List<Element<?>> civilDocs, List<Element<?>> gaDocs) {
        if (gaDocs.size() == 1 && checkIfDocumentExists(civilDocs, gaDocs) < 1) {
            civilDocs.addAll(gaDocs);
        }
    }

    private void addDocumentsByMissingId(List<Element<?>> civilDocs, List<Element<?>> gaDocs) {
        List<UUID> ids = civilDocs.stream().map(Element::getId).toList();
        for (Element<?> gaDoc : gaDocs) {
            if (!ids.contains(gaDoc.getId())) {
                civilDocs.add(gaDoc);
            }
        }
    }

    protected int checkIfDocumentExists(List<Element<?>> civilCaseDocumentList,
                                        List<Element<?>> gaCaseDocumentlist) {

        if (gaCaseDocumentlist.getFirst().getValue().getClass().equals(CaseDocument.class)) {
            List<Element<CaseDocument>> civilCaseList = castElements(civilCaseDocumentList);
            List<Element<CaseDocument>> gaCaseList = castElements(gaCaseDocumentlist);
            return countMatchingDocuments(
                civilCaseList,
                gaCaseList,
                this::getCaseDocumentUrl
            );
        }
        List<Element<Document>> civilCaseList = castElements(civilCaseDocumentList);
        List<Element<Document>> gaCaseList = castElements(gaCaseDocumentlist);
        return countMatchingDocuments(
            civilCaseList,
            gaCaseList,
            Document::getDocumentUrl
        );
    }

    @SuppressWarnings("unchecked")
    private <T> List<Element<T>> castElements(List<Element<?>> documentList) {
        return documentList.stream()
            .map(element -> (Element<T>) element)
            .toList();
    }

    private <T> int countMatchingDocuments(List<Element<T>> civilCaseList,
                                           List<Element<T>> gaCaseList,
                                           Function<T, String> documentUrlExtractor) {
        return (int) civilCaseList.stream()
            .filter(civilDocument -> gaCaseList.parallelStream().anyMatch(
                gaDocument -> documentUrlExtractor.apply(gaDocument.getValue())
                    .equals(documentUrlExtractor.apply(civilDocument.getValue()))
            ))
            .count();
    }

    private String getCaseDocumentUrl(CaseDocument caseDocument) {
        return caseDocument.getDocumentLink().getDocumentUrl();
    }

    private List<Element<GeneralApplicationsDetails>> updateGaApplicationState(GeneralApplicationCaseData caseData, String newState,
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
        log.info("Completed updateGaApplicationState for caseId {}", caseData.getCcdCaseReference());
        return generalApplications;
    }

    private List<Element<GeneralApplicationsDetails>> updateJudgeGaApplicationState(GeneralApplicationCaseData caseData, String newState,
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
        GeneralApplication applicationBuilder = generalApplication.copy();

        applicationBuilder
            .setGeneralAppRespondentAgreement(new GARespondentOrderAgreement())
            .setGeneralAppPBADetails(new GAPbaDetails())
            .setGeneralAppDetailsOfOrder(EMPTY)
            .setGeneralAppReasonsOfOrder(EMPTY)
            .setGeneralAppInformOtherParty(new GAInformOtherParty())
            .setGeneralAppUrgencyRequirement(new GAUrgencyRequirement())
            .setGeneralAppStatementOfTruth(new GAStatementOfTruth())
            .setGeneralAppHearingDate(new GAHearingDateGAspec())
            .setGeneralAppApplnSolicitor(new GASolicitorDetailsGAspec())
            .setGeneralAppHearingDetails(new GAHearingDetails())
            .setGaApplicantDisplayName(EMPTY)
            .setCivilServiceUserRoles(new IdamUserDetails())
            .setGeneralAppRespondentSolicitors(Collections.emptyList())
            .setGeneralAppEvidenceDocument(Collections.emptyList())
            .setApplicantPartyName(EMPTY)
            .setClaimant1PartyName(EMPTY)
            .setClaimant2PartyName(EMPTY)
            .setDefendant1PartyName(EMPTY)
            .setIsMultiParty(null)
            .setIsCcmccLocation(null)
            .setCaseAccessCategory(null)
            .setDefendant2PartyName(EMPTY)
            .setGeneralAppSuperClaimType(EMPTY)
            .setCaseManagementCategory(new GACaseManagementCategory())
            .setLocationName(EMPTY)
            .setGeneralAppHearingDate(new GAHearingDateGAspec())
            .setCertOfSC(new CertOfSC())
            .setApplicantPartyName(EMPTY);

        return applicationBuilder;
    }

    private List<Element<GeneralApplication>> addApplication(
        Optional<Element<GeneralApplication>> newApplicationElement, GeneralApplication application,
                                                             List<Element<GeneralApplication>>
                                                                 generalApplicationsList) {
        List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.addAll(generalApplicationsList);
        Element<GeneralApplication> elementToAdd;
        if (newApplicationElement.isPresent()) {
            elementToAdd = new Element<GeneralApplication>()
                .setId(newApplicationElement.get().getId())
                .setValue(application);
        } else {
            elementToAdd = element(application);
        }
        newApplication.add(elementToAdd);

        return newApplication;
    }

    private List<Element<GADetailsRespondentSol>> updateGaDetailsRespondentCollection(List<Element<GADetailsRespondentSol>> respondentDetails,
                                                                                      String newState,
                                                                                      String applicationId,
                                                                                      String collectionName) {
        log.info("Starting update for {} collection with applicationId: {}", collectionName, applicationId);
        if (!isEmpty(respondentDetails)
            && respondentDetails.stream().anyMatch(respondent -> gaRespSolAppFilterCriteria(respondent, applicationId))) {
            log.info("Matching entry found for applicationId: {}. Updating case state to: {}", applicationId, newState);
            respondentDetails.stream()
                .filter(respondent -> gaRespSolAppFilterCriteria(respondent, applicationId))
                .findAny()
                .orElseThrow(IllegalArgumentException::new)
                .getValue().setCaseState(newState);
            log.info("Case state updated to {} for {} collection with applicationId: {}", newState, collectionName, applicationId);
        }
        return respondentDetails;
    }

    private GeneralApplicationsDetails createGeneralApplicationsDetails(GeneralApplicationsDetails source, Long caseReference) {
        return new GeneralApplicationsDetails()
            .setGeneralApplicationType(source.getGeneralApplicationType())
            .setGeneralAppSubmittedDateGAspec(source.getGeneralAppSubmittedDateGAspec())
            .setCaseLink(new CaseLink(String.valueOf(caseReference)))
            .setParentClaimantIsApplicant(source.getParentClaimantIsApplicant());
    }

    private GeneralApplicationsDetails createGeneralApplicationsDetails(GADetailsRespondentSol source, Long caseReference) {
        return new GeneralApplicationsDetails()
            .setGeneralApplicationType(source.getGeneralApplicationType())
            .setGeneralAppSubmittedDateGAspec(source.getGeneralAppSubmittedDateGAspec())
            .setCaseLink(new CaseLink(String.valueOf(caseReference)))
            .setParentClaimantIsApplicant(source.getParentClaimantIsApplicant());
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

    private Map<String, Object> getUpdatedCaseData(GeneralApplicationCaseData caseData,
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

    private Map<String, Object> getUpdateCaseDataForCollection(GeneralApplicationCaseData caseData,
                                                               List<Element<GeneralApplicationsDetails>>
                                                                   gaDetailsTranslationCollection) {
        Map<String, Object> output = caseData.toMap(mapper);
        output.put(GENERAL_APPLICATIONS_DETAILS_FOR_WELSH, gaDetailsTranslationCollection);
        return output;
    }

    private record ParentCollections(
        List<Element<GeneralApplicationsDetails>> claimantDetails,
        List<Element<GADetailsRespondentSol>> respondentDetails,
        List<Element<GADetailsRespondentSol>> respondentTwoDetails,
        List<Element<GeneralApplicationsDetails>> judgeDetails
    ) {
    }

}
