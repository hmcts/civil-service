package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.JudicialReferralUtils;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToApplicantExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToApplicantWitnesses;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class RespondToDefenceCallbackHandler extends CallbackHandler implements ExpertsValidator, WitnessesValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CLAIMANT_RESPONSE);
    private final ExitSurveyContentService exitSurveyContentService;
    private final UnavailableDateValidator unavailableDateValidator;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final FeatureToggleService featureToggleService;
    private final LocationReferenceDataService locationRefDataService;
    private final LocationRefDataUtil locationRefDataUtil;
    private final LocationHelper locationHelper;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final ToggleConfiguration toggleConfiguration;
    private final AssignCategoryId assignCategoryId;
    private final CaseDetailsConverter caseDetailsConverter;
    private final FrcDocumentsUtils frcDocumentsUtils;
    @Value("${court-location.unspecified-claim.epimms-id}")
    String ccmccEpimsId;
    private final Optional<UpdateWaCourtLocationsService> updateWaCourtLocationsService;
    private final RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateClaimantResponseScenarioFlag,
            callbackKey(MID, "set-applicants-proceed-intention"), this::setApplicantsProceedIntention,
            callbackKey(MID, "experts"), this::validateApplicantExperts,
            callbackKey(MID, "witnesses"), this::validateApplicantWitnesses,
            callbackKey(MID, "validate-unavailable-dates"), this::validateUnavailableDates,
            callbackKey(MID, "statement-of-truth"), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse populateClaimantResponseScenarioFlag(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        caseData.setClaimantResponseScenarioFlag(getMultiPartyScenario(caseData));
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);
        caseData.setFeatureToggleWA(toggleConfiguration.getFeatureToggle());

        // add document from defendant response documents, to placeholder field for preview during event.
        caseData.getDefendantResponseDocuments().forEach(document -> {
            if (document.getValue().getDocumentType().equals(DocumentType.DEFENDANT_DEFENCE)
                && document.getValue().getCreatedBy().equals("Defendant")) {
                caseData.setRespondent1ClaimResponseDocument(ResponseDocument.builder()
                                                                 .file(document.getValue().getDocumentLink())
                                                                 .build());
            }
            if (document.getValue().getDocumentType().equals(DocumentType.DEFENDANT_DEFENCE)
                && document.getValue().getCreatedBy().equals("Defendant 2")) {
                caseData.setRespondent2ClaimResponseDocument(ResponseDocument.builder()
                                                                 .file(document.getValue().getDocumentLink())
                                                                 .build());
            }
            if ((getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP)) {
                caseData.setRespondentSharedClaimResponseDocument(ResponseDocument.builder()
                                                                      .file(document.getValue().getDocumentLink())
                                                                      .build());
            }
        });

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateApplicantWitnesses(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (YES.equals(caseData.getClaimant2ResponseFlag())) {
            return validateWitnesses(callbackParams.getCaseData().getApplicant2DQ());
        }
        return validateWitnesses(callbackParams.getCaseData().getApplicant1DQ());
    }

    private CallbackResponse validateApplicantExperts(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (YES.equals(caseData.getClaimant2ResponseFlag())) {
            return validateExperts(callbackParams.getCaseData().getApplicant2DQ());
        }
        return validateExperts(callbackParams.getCaseData().getApplicant1DQ());
    }

    private CallbackResponse setApplicantsProceedIntention(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        final MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        caseData.setApplicantsProceedIntention(NO);
        caseData.setClaimantResponseDocumentToDefendant2Flag(NO);
        caseData.setClaimant2ResponseFlag(NO);

        if (anyApplicantDecidesToProceedWithClaim(caseData)) {
            caseData.setApplicantsProceedIntention(YES);
        }

        if ((multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP
            && YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()))
            || (multiPartyScenario == ONE_V_TWO_ONE_LEGAL_REP
            && YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2())
            && NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()))) {
            caseData.setClaimantResponseDocumentToDefendant2Flag(YES);
        }

        if (multiPartyScenario == TWO_V_ONE && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())
            && NO.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())) {
            caseData.setClaimant2ResponseFlag(YES);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private boolean anyApplicantDecidesToProceedWithClaim(CaseData caseData) {
        return YES.equals(caseData.getApplicant1ProceedWithClaim())
            || YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
            || YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())
            || YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
            || YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2());
    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Hearing hearing;
        if (YES.equals(caseData.getClaimant2ResponseFlag())) {
            hearing = caseData.getApplicant2DQ().getHearing();
        } else {
            hearing = caseData.getApplicant1DQ().getHearing();
        }
        List<String> errors = unavailableDateValidator.validate(hearing);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime currentTime = time.now();
        caseData.setBusinessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE));
        caseData.setApplicant1ResponseDate(currentTime);

        log.info(
            "Case management location for {} is {}",
            caseData.getLegacyCaseReference(),
            caseData.getCaseManagementLocation()
        );

        // When a case has been transferred, we do not update the location using claimant/defendant preferred location logic
        if (notTransferredOnline(caseData)) {
            updateCaseManagementLocation(callbackParams, caseData);
        }

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario == TWO_V_ONE) {
            caseData.setApplicant2ResponseDate(currentTime);
        }

        if (anyApplicantDecidesToProceedWithClaim(caseData)) {
            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();

            updateApplicants(caseData, statementOfTruth, callbackParams);

            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            caseData.setUiStatementOfTruth(StatementOfTruth.builder().build());
        }

        assembleResponseDocuments(caseData);
        frcDocumentsUtils.assembleClaimantsFRCDocuments(caseData);
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(caseData);

        addEventAndDateAddedToApplicantExperts(caseData);
        addEventAndDateAddedToApplicantWitnesses(caseData);
        populateDQPartyIds(caseData);

        caseFlagsInitialiser.initialiseCaseFlags(CLAIMANT_RESPONSE, caseData);

        if (multiPartyScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            caseData.setRespondentSharedClaimResponseDocument(null);
        }

        //Set to null because there are no more deadlines
        caseData.setNextDeadline(null);

        // null/delete the document used for preview, otherwise it will show as duplicate within case file view
        // and documents are added to claimantUploads, if we do not remove/null the original,
        caseData.setApplicant1DefenceResponseDocument(null);
        caseData.setRespondent1ClaimResponseDocument(null);
        caseData.setRespondentSharedClaimResponseDocument(null);
        if (caseData.getApplicant1DQ() != null
            && caseData.getApplicant1DQ() != null) {
            caseData.getApplicant1DQ().setApplicant1DQDraftDirections(null);
        }
        if (caseData.getApplicant2DQ() != null
            && caseData.getApplicant2DQ() != null) {
            caseData.getApplicant2DQ().setApplicant2DQDraftDirections(null);
        }

        if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(
                callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                caseData
            ));
        }

        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabApplicant(callbackParams, caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .state((JudicialReferralUtils.shouldMoveToJudicialReferral(
                caseData,
                featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            )
                ? CaseState.JUDICIAL_REFERRAL
                : CaseState.PROCEEDS_IN_HERITAGE_SYSTEM).name())
            .build();
    }

    private void updateApplicants(CaseData caseData, StatementOfTruth statementOfTruth,
                                  CallbackParams callbackParams) {
        if (caseData.getApplicant1DQ() != null
            && caseData.getApplicant1DQ().getApplicant1DQFileDirectionsQuestionnaire() != null) {
            Applicant1DQ applicant1DQ = caseData.getApplicant1DQ();
            applicant1DQ.setApplicant1DQStatementOfTruth(statementOfTruth);

            String responseCourtCode = locationRefDataUtil.getPreferredCourtData(
                caseData,
                callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(), true
            );
            applicant1DQ.setApplicant1DQRequestedCourt(
                RequestedCourt.builder()
                    .caseLocation(caseData.getCourtLocation().getCaseLocation())
                    .responseCourtCode(responseCourtCode)
                    .build());
        }

        if (caseData.getApplicant2DQ() != null
            && caseData.getApplicant2DQ().getApplicant2DQFileDirectionsQuestionnaire() != null) {
            Applicant2DQ applicant2DQ = caseData.getApplicant2DQ();
            applicant2DQ.setApplicant2DQStatementOfTruth(statementOfTruth);
        }
    }

    private void updateCaseManagementLocation(CallbackParams callbackParams, CaseData caseData) {
        Optional<RequestedCourt> preferredCourt = locationHelper.getCaseManagementLocation(caseData);
        preferredCourt.map(RequestedCourt::getCaseLocation)
            .ifPresent(caseData::setCaseManagementLocation);

        locationHelper.getCaseManagementLocation(caseData)
            .ifPresent(requestedCourt -> locationHelper.updateCaseManagementLocation(
                caseData,
                requestedCourt,
                () -> locationRefDataService.getCourtLocationsForDefaultJudgments(callbackParams.getParams().get(
                    CallbackParams.Params.BEARER_TOKEN).toString())
            ));

        if (log.isDebugEnabled()) {
            log.debug(
                "Case management location for {} is {}",
                caseData.getLegacyCaseReference(),
                caseData.getCaseManagementLocation()
            );
        }
    }

    static final String CLAIMANT = "Claimant";

    private void assembleResponseDocuments(CaseData caseData) {
        List<Element<CaseDocument>> claimantUploads = new ArrayList<>();
        Optional.ofNullable(caseData.getApplicant1DefenceResponseDocument())
            .map(ResponseDocument::getFile).ifPresent(claimDocument -> claimantUploads.add(
                buildElemCaseDocument(
                    claimDocument, CLAIMANT,
                    caseData.getApplicant1ResponseDate(), DocumentType.CLAIMANT_DEFENCE
                )));
        Optional.ofNullable(caseData.getClaimantDefenceResDocToDefendant2())
            .map(ResponseDocument::getFile).ifPresent(claimDocument -> claimantUploads.add(
                buildElemCaseDocument(
                    claimDocument, CLAIMANT,
                    caseData.getApplicant1ResponseDate(), DocumentType.CLAIMANT_DEFENCE
                )));
        Optional.ofNullable(caseData.getApplicant1DQ())
            .map(Applicant1DQ::getApplicant1DQDraftDirections)
            .ifPresent(document -> claimantUploads.add(
                buildElemCaseDocument(
                    document, CLAIMANT,
                    caseData.getApplicant1ResponseDate(),
                    DocumentType.CLAIMANT_DRAFT_DIRECTIONS
                )));
        Optional.ofNullable(caseData.getApplicant2DQ())
            .map(Applicant2DQ::getApplicant2DQDraftDirections)
            .ifPresent(document -> claimantUploads.add(
                buildElemCaseDocument(
                    document, CLAIMANT,
                    caseData.getApplicant2ResponseDate(),
                    DocumentType.CLAIMANT_DRAFT_DIRECTIONS
                )));
        List<Element<CaseDocument>> duplicateClaimantDefendantResponseDocs = caseData.getDuplicateClaimantDefendantResponseDocs();
        if (!claimantUploads.isEmpty()) {
            assignCategoryId.assignCategoryIdToCollection(
                claimantUploads,
                document -> document.getValue().getDocumentLink(),
                DocCategory.APP1_DQ.getValue()
            );
            List<Element<CaseDocument>> copy = assignCategoryId.copyCaseDocumentListWithCategoryId(
                claimantUploads, DocCategory.DQ_APP1.getValue());
            if (Objects.nonNull(copy)) {
                duplicateClaimantDefendantResponseDocs.addAll(copy);
            }
            caseData.setClaimantResponseDocuments(claimantUploads);
            caseData.setDuplicateClaimantDefendantResponseDocs(copy);
        }
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        String claimNumber = caseData.getLegacyCaseReference();
        String title;
        String body = format("<br />We will review the case and contact you to tell you what to do next.%n%n");

        switch (multiPartyScenario) {
            case TWO_V_ONE:
                // XOR: If they are the opposite of each other - Divergent response
                if (YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                    ^ YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())) {
                    title = "# You have chosen to proceed with the claim against one defendant only%n"
                        + "## Claim number: %s";
                    break;
                }
                // FALL-THROUGH
            case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP:
                // XOR: If they are the opposite of each other - Divergent response
                if (YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                    ^ YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2())) {
                    title = "# You have chosen to proceed with the claim against one defendant only%n"
                        + "## Claim number: %s";
                    break;
                }
                // FALL-THROUGH
            default: {
                //Non-divergent and there is at least one yes to proceed with claim
                if (anyApplicantDecidesToProceedWithClaim(caseData)) {
                    title = "# You have chosen to proceed with the claim%n## Claim number: %s";
                    break;
                }
                //All applicants chose not to proceed
                title = "# You have chosen not to proceed with the claim%n## Claim number: %s";
                body = "<br />If you do want to proceed you need to do it within:"
                    + "<ul><li>14 days if the claim is allocated to a small claims track</li>"
                    + "<li>28 days if the claim is allocated to a fast or multi track</li></ul>"
                    + "<p>The case will be stayed if you do not proceed within the allowed timescale.</p>";
            }
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format(title, claimNumber))
            .confirmationBody(body + exitSurveyContentService.applicantSurvey())
            .build();
    }

    public boolean notTransferredOnline(CaseData caseData) {
        return caseData.getCaseManagementLocation().getBaseLocation().equals(ccmccEpimsId);
    }
}
