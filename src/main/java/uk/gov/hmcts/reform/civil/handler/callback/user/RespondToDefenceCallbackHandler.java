package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.getAllocatedTrack;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;

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
    private final LocationRefDataService locationRefDataService;
    private final LocationRefDataUtil locationRefDataUtil;
    private final LocationHelper locationHelper;
    private final CaseFlagsInitialiser caseFlagsInitialiser;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateClaimantResponseScenarioFlag,
            callbackKey(V_1, ABOUT_TO_START), this::populateClaimantResponseScenarioFlag,
            callbackKey(MID, "set-applicants-proceed-intention"), this::setApplicantsProceedIntention,
            callbackKey(MID, "experts"), this::validateApplicantExperts,
            callbackKey(MID, "witnesses"), this::validateApplicantWitnesses,
            callbackKey(MID, "validate-unavailable-dates"), this::validateUnavailableDates,
            callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth,
            callbackKey(ABOUT_TO_SUBMIT), params -> aboutToSubmit(params, false),
            callbackKey(V_1, ABOUT_TO_SUBMIT), params -> aboutToSubmit(params, true),
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse populateClaimantResponseScenarioFlag(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();

        updatedData.claimantResponseScenarioFlag(getMultiPartyScenario(caseData))
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM);

        if ((getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP)) {
            updatedData.respondentSharedClaimResponseDocument(caseData.getRespondent1ClaimResponseDocument());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
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
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        CaseData.CaseDataBuilder updatedData =
            caseData.toBuilder()
                .applicantsProceedIntention(NO)
                .claimantResponseDocumentToDefendant2Flag(NO)
                .claimant2ResponseFlag(NO);

        if (anyApplicantDecidesToProceedWithClaim(caseData)) {
            updatedData.applicantsProceedIntention(YES);
        }

        if ((multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP
            && YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()))
            || (multiPartyScenario == ONE_V_TWO_ONE_LEGAL_REP
            && YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2())
            && NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()))) {
            updatedData.claimantResponseDocumentToDefendant2Flag(YES);
        }

        if (multiPartyScenario == TWO_V_ONE && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())
            && NO.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())) {
            updatedData.claimant2ResponseFlag(YES);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
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

    private CallbackResponse resetStatementOfTruth(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        // resetting statement of truth field, this resets in the page, but the data is still sent to the db.
        // setting null here does not clear, need to overwrite with value.
        // must be to do with the way XUI cache data entered through the lifecycle of an event.
        CaseData updatedCaseData = caseData.toBuilder()
            .uiStatementOfTruth(StatementOfTruth.builder().role("").build())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams, boolean v1) {
        CaseData caseData = callbackParams.getCaseData();

        LocalDateTime currentTime = time.now();

        CaseData.CaseDataBuilder builder = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE))
            .applicant1ResponseDate(currentTime);

        if (log.isDebugEnabled()) {
            log.debug("Case management location for " + caseData.getLegacyCaseReference()
                          + " is " + builder.build().getCaseManagementLocation());
        }

        if (v1) {
            updateCaseManagementLocation(callbackParams, builder);
        }

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario == TWO_V_ONE) {
            builder.applicant2ResponseDate(currentTime);
        }

        if (anyApplicantDecidesToProceedWithClaim(caseData)) {
            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();

            if (caseData.getApplicant1DQ() != null
                && caseData.getApplicant1DQ().getApplicant1DQFileDirectionsQuestionnaire() != null) {
                Applicant1DQ.Applicant1DQBuilder applicant1DQBuilder = caseData.getApplicant1DQ().toBuilder();
                applicant1DQBuilder.applicant1DQStatementOfTruth(statementOfTruth);

                if (featureToggleService.isCourtLocationDynamicListEnabled()) {

                    String responseCourtCode = locationRefDataUtil.getPreferredCourtData(
                        caseData,
                        CallbackParams.Params.BEARER_TOKEN.toString(), true
                    );
                    applicant1DQBuilder.applicant1DQRequestedCourt(
                        RequestedCourt.builder()
                            .caseLocation(caseData.getCourtLocation().getCaseLocation())
                            .responseCourtCode(responseCourtCode)
                            .build());
                }

                builder.applicant1DQ(applicant1DQBuilder.build());
            }

            if (caseData.getApplicant2DQ() != null
                && caseData.getApplicant2DQ().getApplicant2DQFileDirectionsQuestionnaire() != null) {
                Applicant2DQ dq = caseData.getApplicant2DQ().toBuilder()
                    .applicant2DQStatementOfTruth(statementOfTruth)
                    .build();

                builder.applicant2DQ(dq);
            }

            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            builder.uiStatementOfTruth(StatementOfTruth.builder().build());
        }

        assembleResponseDocuments(caseData, builder);

        caseFlagsInitialiser.initialiseCaseFlags(CLAIMANT_RESPONSE, builder);

        if (multiPartyScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            builder.respondentSharedClaimResponseDocument(null);
        }

        //Set to null because there are no more deadlines
        builder.nextDeadline(null);
        AboutToStartOrSubmitCallbackResponse response = null;

        AllocatedTrack allocatedTrack =
            getAllocatedTrack(caseData.getClaimValue().toPounds(), caseData.getClaimType());

        if (v1 && featureToggleService.isSdoEnabled()
            && !AllocatedTrack.MULTI_CLAIM.equals(allocatedTrack)) {
            if (caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)) {
                if ((multiPartyScenario.equals(ONE_V_ONE) || multiPartyScenario.equals(TWO_V_ONE))
                    || multiPartyScenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
                    response = AboutToStartOrSubmitCallbackResponse.builder()
                        .data(builder.build().toMap(objectMapper))
                        .state(CaseState.JUDICIAL_REFERRAL.name())
                        .build();
                } else if (multiPartyScenario.equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                    if (caseData.getRespondent2ClaimResponseType()
                        .equals(RespondentResponseType.FULL_DEFENCE)) {
                        response = AboutToStartOrSubmitCallbackResponse.builder()
                            .data(builder.build().toMap(objectMapper))
                            .state(CaseState.JUDICIAL_REFERRAL.name())
                            .build();
                    }
                } else {
                    response = AboutToStartOrSubmitCallbackResponse.builder()
                        .data(builder.build().toMap(objectMapper))
                        .build();
                }
            } else {
                response = AboutToStartOrSubmitCallbackResponse.builder()
                    .data(builder.build().toMap(objectMapper))
                    .build();
            }
        } else {
            response = AboutToStartOrSubmitCallbackResponse.builder()
                .data(builder.build().toMap(objectMapper))
                .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                .build();
        }

        return response;
    }

    private void updateCaseManagementLocation(CallbackParams callbackParams,
                                              CaseData.CaseDataBuilder builder) {
        CaseData caseData = callbackParams.getCaseData();
        Optional<RequestedCourt> preferredCourt = locationHelper.getCaseManagementLocation(caseData);
        preferredCourt.map(RequestedCourt::getCaseLocation)
            .ifPresent(builder::caseManagementLocation);

        locationHelper.getCaseManagementLocation(caseData)
            .ifPresent(requestedCourt -> locationHelper.updateCaseManagementLocation(
                builder,
                requestedCourt,
                () -> locationRefDataService.getCourtLocationsForDefaultJudgments(callbackParams.getParams().get(
                    CallbackParams.Params.BEARER_TOKEN).toString())
            ));
        if (log.isDebugEnabled()) {
            log.debug("Case management location for " + caseData.getLegacyCaseReference()
                          + " is " + builder.build().getCaseManagementLocation());
        }
    }

    private void assembleResponseDocuments(CaseData caseData, CaseData.CaseDataBuilder updatedCaseData) {
        List<Element<CaseDocument>> claimantUploads = new ArrayList<>();
        Optional.ofNullable(caseData.getApplicant1DefenceResponseDocument())
            .map(ResponseDocument::getFile).ifPresent(claimDocument -> claimantUploads.add(
                buildElemCaseDocument(claimDocument, "Claimant",
                                      updatedCaseData.build().getApplicant1ResponseDate(), DocumentType.CLAIMANT_DEFENCE
                )));
        Optional.ofNullable(caseData.getClaimantDefenceResDocToDefendant2())
            .map(ResponseDocument::getFile).ifPresent(claimDocument -> claimantUploads.add(
                buildElemCaseDocument(claimDocument, "Claimant",
                                      updatedCaseData.build().getApplicant1ResponseDate(), DocumentType.CLAIMANT_DEFENCE
                )));
        Optional.ofNullable(caseData.getApplicant1DQ())
            .map(Applicant1DQ::getApplicant1DQDraftDirections)
            .ifPresent(document -> claimantUploads.add(
                buildElemCaseDocument(document, "Claimant",
                                      updatedCaseData.build().getApplicant1ResponseDate(),
                                      DocumentType.CLAIMANT_DRAFT_DIRECTIONS
                )));
        Optional.ofNullable(caseData.getApplicant2DQ())
            .map(Applicant2DQ::getApplicant2DQDraftDirections)
            .ifPresent(document -> claimantUploads.add(
                buildElemCaseDocument(document, "Claimant",
                                      updatedCaseData.build().getApplicant2ResponseDate(),
                                      DocumentType.CLAIMANT_DRAFT_DIRECTIONS
                )));
        if (!claimantUploads.isEmpty()) {
            updatedCaseData.claimantResponseDocuments(claimantUploads);
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
            case ONE_V_TWO_ONE_LEGAL_REP:
            case ONE_V_TWO_TWO_LEGAL_REP:
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
}
