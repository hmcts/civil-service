package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.CaseDataToTextGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.PaymentDateService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizenui.RespondentMediationService;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.JudicialReferralUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.model.dq.Expert.fromSmallClaimExpertDetails;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToApplicantExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToApplicantWitnesses;

@Service
@RequiredArgsConstructor
@Slf4j
public class RespondToDefenceSpecCallbackHandler extends CallbackHandler
    implements ExpertsValidator, WitnessesValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CLAIMANT_RESPONSE_SPEC);
    private final ObjectMapper objectMapper;
    private final Time time;
    private final UnavailableDateValidator unavailableDateValidator;
    private final List<RespondToResponseConfirmationHeaderGenerator> confirmationHeaderGenerators;
    private final List<RespondToResponseConfirmationTextGenerator> confirmationTextGenerators;
    private final LocationRefDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;
    private final FeatureToggleService featureToggleService;
    private final JudgementService judgementService;
    private final LocationHelper locationHelper;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private static final String datePattern = "dd MMMM yyyy";
    private final RespondentMediationService respondentMediationService;
    private final PaymentDateService paymentDateService;
    private final ResponseOneVOneShowTagService responseOneVOneShowTagService;
    private final DeadlineExtensionCalculatorService deadlineCalculatorService;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(MID, "experts"), this::validateApplicantExperts)
            .put(callbackKey(MID, "witnesses"), this::validateApplicantWitnesses)
            .put(callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth)
            .put(callbackKey(MID, "validate-unavailable-dates"), this::validateUnavailableDates)
            .put(callbackKey(MID, "set-applicant1-proceed-flag"), this::setApplicant1ProceedFlag)
            .put(callbackKey(V_1, MID, "set-applicant-route-flags"), this::setApplicantRouteFlags)
            .put(callbackKey(V_2, MID, "set-applicant-route-flags"), this::setApplicantRouteFlags)
            .put(callbackKey(V_1, MID, "validate-respondent-payment-date"), this::validatePaymentDate)
            .put(callbackKey(V_1, MID, "get-payment-date"), this::getPaymentDate)
            .put(callbackKey(V_1, MID, "validate-suggest-instalments"), this::suggestInstalmentsValidation)
            .put(callbackKey(V_1, MID, "validate-amount-paid"), this::validateAmountPaid)
            .put(callbackKey(V_1, MID, "set-up-ccj-amount-summary"), this::buildJudgmentAmountSummaryDetails)
            .put(callbackKey(V_1, MID, "set-mediation-show-tag"), this::setMediationShowTag)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit)
            .put(callbackKey(V_2, ABOUT_TO_SUBMIT), this::aboutToSubmit)
            .put(callbackKey(ABOUT_TO_START), this::populateCaseData)
            .put(callbackKey(V_2, ABOUT_TO_START), this::populateCaseData)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> errors;
        if (SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack())) {
            SmallClaimHearing smallClaimHearing = caseData.getApplicant1DQ().getApplicant1DQSmallClaimHearing();
            errors = unavailableDateValidator.validateSmallClaimsHearing(smallClaimHearing);
        } else {
            Hearing hearingLRspec = caseData.getApplicant1DQ().getApplicant1DQHearingLRspec();
            errors = unavailableDateValidator.validateFastClaimHearing(hearingLRspec);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateApplicantWitnesses(CallbackParams callbackParams) {
        return validateWitnesses(callbackParams.getCaseData().getApplicant1DQ());
    }

    private CallbackResponse validateApplicantExperts(CallbackParams callbackParams) {
        return validateExperts(callbackParams.getCaseData().getApplicant1DQ());
    }

    private CallbackResponse setApplicant1ProceedFlag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        setApplicant1ProceedFlagToYes(caseData, caseDataBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void setApplicant1ProceedFlagToYes(CaseData caseData, CaseData.CaseDataBuilder caseDataBuilder) {

        if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))
            && YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
            caseDataBuilder.applicant1ProceedWithClaim(YES);
        }
    }

    private void setApplicantDefenceResponseDocFlag(CaseData caseData, CaseData.CaseDataBuilder caseDataBuilder) {
        caseDataBuilder.applicantDefenceResponseDocumentAndDQFlag(caseData.doesPartPaymentRejectedOrItsFullDefenceResponse());
    }

    private CallbackResponse setApplicantRouteFlags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        setApplicantDefenceResponseDocFlag(caseData, caseDataBuilder);
        setApplicant1ProceedFlagToYes(caseData, caseDataBuilder);
        setMediationConditionFlag(caseData, caseDataBuilder);

        if (V_2.equals(callbackParams.getVersion()) && shouldVulnerabilityAppear(caseData)) {
            setVulnerabilityFlag(caseData, caseDataBuilder);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void setVulnerabilityFlag(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        caseData.getShowConditionFlags().add(DefendantResponseShowTag.VULNERABILITY);
        updatedCaseData.showConditionFlags(caseData.getShowConditionFlags());
    }

    private void setMediationConditionFlag(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (respondentMediationService.setMediationRequired(caseData) != null) {
            caseData.getShowConditionFlags().add(respondentMediationService.setMediationRequired(caseData));
            updatedCaseData.showConditionFlags(caseData.getShowConditionFlags());
        }
    }

    private CallbackResponse setMediationShowTag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        setMediationConditionFlag(caseData, caseDataBuilder);
        setApplicantDefenceResponseDocFlag(caseData, caseDataBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    /**
     * Checks if the vulnerability flag should be added to case data.
     *
     * @param caseData current case data
     * @return true if and only if either of the following conditions are satisfied: (a) applicant does not
     *     accept the amount the defendant admitted owing, or (b) defendant rejects the whole claim and applicant
     *     wants to proceed with the claim
     */
    private boolean shouldVulnerabilityAppear(CaseData caseData) {
        return (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
            && caseData.getApplicant1ProceedWithClaim() == YES)
            || caseData.getApplicant1AcceptAdmitAmountPaidSpec() == NO;
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

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE_SPEC))
            .applicant1ResponseDate(time.now());

        // null/delete the document used for preview, otherwise it will show as duplicate within case file view
        if (featureToggleService.isCaseFileViewEnabled()) {
            builder.respondent1GeneratedResponseDocument(null);
            builder.respondent2GeneratedResponseDocument(null);
            builder.respondent1ClaimResponseDocumentSpec(null);
        }

        // if its flight delay claim set the claim type to Flight delay for Work Allocation
        if (isFlightDelayClaim(caseData)) {
            builder.claimType(ClaimType.FLIGHT_DELAY);
        }

        //Update the caseManagement location to the flight location if No flight location update to Claimant
        // preferred location
        if (isFlightDelayClaimAndUpdateCaseLocation(caseData)) {
            builder.caseManagementLocation(caseData.getFlightDelayDetails().getFlightCourtLocation());
        } else if (isFlightDelayClaim(caseData) == false) {
            locationHelper.getCaseManagementLocation(caseData)
                .ifPresent(requestedCourt -> locationHelper.updateCaseManagementLocation(
                    builder,
                    requestedCourt,
                    () -> locationRefDataService.getCourtLocationsForDefaultJudgments(callbackParams.getParams().get(
                        CallbackParams.Params.BEARER_TOKEN).toString())
                ));
        }

        if (log.isDebugEnabled()) {
            log.debug("Case management location for " + caseData.getLegacyCaseReference()
                          + " is " + builder.build().getCaseManagementLocation());
        }

        if (caseData.hasApplicantProceededWithClaim() || (caseData.isPartAdmitClaimSpec() && caseData.isPartAdmitClaimNotSettled())) {
            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Applicant1DQ.Applicant1DQBuilder dq = caseData.getApplicant1DQ().toBuilder()
                .applicant1DQStatementOfTruth(statementOfTruth);

            if (isFlightDelayClaim(caseData) == false
                || isFlightDelayClaimAndUpdateCaseLocationWithClaimantCourt(caseData)) {
                updateDQCourtLocations(callbackParams, caseData, builder, dq);
            }

            var smallClaimWitnesses = builder.build().getApplicant1DQWitnessesSmallClaim();
            if (smallClaimWitnesses != null) {
                dq.applicant1DQWitnesses(smallClaimWitnesses);
            }

            builder.applicant1DQ(dq.build());
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            builder.uiStatementOfTruth(StatementOfTruth.builder().build());
        }

        if (caseData.getApplicant1DQ() != null
            && caseData.getApplicant1DQ().getSmallClaimExperts() != null) {
            Expert expert = fromSmallClaimExpertDetails(caseData.getApplicant1DQ().getSmallClaimExperts());
            YesOrNo expertRequired = TWO_V_ONE.equals(getMultiPartyScenario(caseData)) ? caseData.getApplicantMPClaimExpertSpecRequired()
                : caseData.getApplicant1ClaimExpertSpecRequired();
            builder.applicant1DQ(
                builder.build().getApplicant1DQ().toBuilder()
                    .applicant1DQExperts(Experts.builder()
                                             .expertRequired(expertRequired)
                                             .details(wrapElements(expert))
                                             .build())
                    .build());
        }

        if (caseData.getApplicant2DQ() != null
            && caseData.getApplicant2DQ().getSmallClaimExperts() != null) {
            Expert expert = fromSmallClaimExpertDetails(caseData.getApplicant2DQ().getSmallClaimExperts());
            builder.applicant2DQ(
                builder.build().getApplicant2DQ().toBuilder()
                    .applicant2DQExperts(Experts.builder()
                                             .expertRequired(caseData.getApplicantMPClaimExpertSpecRequired())
                                             .details(wrapElements(expert))
                                             .build())
                    .build());
        }

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(builder,
                                                                       featureToggleService.isUpdateContactDetailsEnabled());

        if (featureToggleService.isUpdateContactDetailsEnabled()) {
            addEventAndDateAddedToApplicantExperts(builder);
            addEventAndDateAddedToApplicantWitnesses(builder);
        }

        if (featureToggleService.isHmcEnabled()) {
            populateDQPartyIds(builder);
        }

        caseFlagsInitialiser.initialiseCaseFlags(CLAIMANT_RESPONSE_SPEC, builder);

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(builder.build().toMap(objectMapper));

        putCaseStateInJudicialReferral(caseData, response);

        if (V_2.equals(callbackParams.getVersion())
            && featureToggleService.isPinInPostEnabled()
            && isOneVOne(caseData)) {
            if (caseData.hasClaimantAgreedToFreeMediation()) {
                response.state(CaseState.IN_MEDIATION.name());
            } else if (caseData.hasApplicantRejectedRepaymentPlan() || caseData.hasApplicantAcceptedRepaymentPlan()) {
                response.state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
            } else if (
                caseData.isClaimantNotSettlePartAdmitClaim()
                    && ((caseData.hasClaimantNotAgreedToFreeMediation()
                    || caseData.hasDefendantNotAgreedToFreeMediation())
                    || caseData.isFastTrackClaim())) {
                response.state(CaseState.JUDICIAL_REFERRAL.name());
            } else if (caseData.isPartAdmitClaimSettled()) {
                response.state(CaseState.CASE_SETTLED.name());
            }
        }
        return response.build();
    }

    private void updateDQCourtLocations(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder, Applicant1DQ.Applicant1DQBuilder dq) {
        handleCourtLocationData(caseData, builder, dq, callbackParams);
        locationHelper.getCaseManagementLocation(builder.applicant1DQ(dq.build()).build())
            .ifPresent(requestedCourt -> locationHelper.updateCaseManagementLocation(
                builder,
                requestedCourt,
                () -> locationRefDataService.getCourtLocationsForDefaultJudgments(
                    callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString())
            ));
        if (log.isDebugEnabled()) {
            log.debug("Case management location for " + caseData.getLegacyCaseReference()
                          + " is " + builder.build().getCaseManagementLocation());
        }
    }

    private void putCaseStateInJudicialReferral(CaseData caseData, AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response) {
        if (caseData.isRespondentResponseFullDefence()
            && JudicialReferralUtils.shouldMoveToJudicialReferral(caseData)) {
            response.state(CaseState.JUDICIAL_REFERRAL.name());
        }
    }

    private void handleCourtLocationData(CaseData caseData, CaseData.CaseDataBuilder dataBuilder,
                                         Applicant1DQ.Applicant1DQBuilder dq,
                                         CallbackParams callbackParams) {
        RequestedCourt requestedCourt = caseData.getApplicant1DQ().getApplicant1DQRequestedCourt();
        if (requestedCourt != null) {
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                fetchLocationData(callbackParams), requestedCourt.getResponseCourtLocations());
            if (Objects.nonNull(courtLocation)) {
                dataBuilder
                    .applicant1DQ(dq.applicant1DQRequestedCourt(
                        caseData.getApplicant1DQ().getApplicant1DQRequestedCourt().toBuilder()
                            .responseCourtLocations(null)
                            .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                            .responseCourtCode(courtLocation.getCourtLocationCode()).build()
                    ).build());
            }
        }
    }

    private CallbackResponse populateCaseData(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        if (isDefendantFullAdmitPayImmediately(caseData)) {
            LocalDate whenBePaid = paymentDateService.getPaymentDateAdmittedClaim(caseData);
            updatedCaseData.showResponseOneVOneFlag(responseOneVOneShowTagService.setUpOneVOneFlow(caseData));
            updatedCaseData.whenToBePaidText(formatLocalDate(whenBePaid, DATE));
        }

        updatedCaseData.respondent1Copy(caseData.getRespondent1())
            .claimantResponseScenarioFlag(getMultiPartyScenario(caseData))
            .caseAccessCategory(CaseCategory.SPEC_CLAIM);

        List<LocationRefData> locations = fetchLocationData(callbackParams);
        updatedCaseData.applicant1DQ(
            Applicant1DQ.builder().applicant1DQRequestedCourt(
                RequestedCourt.builder().responseCourtLocations(
                    courtLocationUtils.getLocationsFromList(locations)).build()
            ).build());

        if (V_2.equals(callbackParams.getVersion()) && featureToggleService.isPinInPostEnabled()) {
            updatedCaseData.showResponseOneVOneFlag(responseOneVOneShowTagService.setUpOneVOneFlow(caseData));
            updatedCaseData.respondent1PaymentDateToStringSpec(setUpPayDateToString(caseData));

            Optional<BigDecimal> howMuchWasPaid = Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                .map(RespondToClaim::getHowMuchWasPaid);

            howMuchWasPaid.ifPresent(howMuchWasPaidValue -> updatedCaseData.partAdmitPaidValuePounds(
                MonetaryConversions.penniesToPounds(howMuchWasPaidValue)));

            updatedCaseData.responseClaimTrack(AllocatedTrack.getAllocatedTrack(
                caseData.getTotalClaimAmount(),
                null
            ).name());
        }
        // add direction questionaire document from system generated documents, to placeholder field for preview during event.
        // Or add sealed response form  from system generated documents, to placeholder field for preview during event.
        populatePreviewDocuments(caseData, updatedCaseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private void populatePreviewDocuments(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        // add direction questionaire document from system generated documents, to placeholder field for preview during event.
        if (caseData.getRespondent2DocumentURL() == null) {
            caseData.getSystemGeneratedCaseDocuments().forEach(document -> {
                if (document.getValue().getDocumentName().contains("defendant_directions_questionnaire_form")) {
                    updatedCaseData.respondent1GeneratedResponseDocument(document.getValue());
                }
            });
        } else {
            caseData.getSystemGeneratedCaseDocuments().forEach(document -> {
                if (document.getValue().getDocumentLink().getDocumentUrl().equals(caseData.getRespondent1DocumentURL())) {
                    updatedCaseData.respondent1GeneratedResponseDocument(document.getValue());
                }
                if (document.getValue().getDocumentLink().getDocumentUrl().equals(caseData.getRespondent2DocumentURL())) {
                    updatedCaseData.respondent2GeneratedResponseDocument(document.getValue());
                }
            });
        }
        // add sealed response form  from system generated documents, to placeholder field for preview during event.
        if (featureToggleService.isPinInPostEnabled()) {
            caseData.getSystemGeneratedCaseDocuments().forEach(document -> {
                if (document.getValue().getDocumentName().contains("response_sealed_form.pdf")) {
                    updatedCaseData.respondent1ClaimResponseDocumentSpec(document.getValue());
                }
            });
        }
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (!AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            caseData.toBuilder().ccdState(CaseState.JUDICIAL_REFERRAL).build();
        } else if (isDefendantFullAdmitPayImmediately(caseData)) {
            caseData.toBuilder().ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM).build();
        }

        SubmittedCallbackResponse.SubmittedCallbackResponseBuilder responseBuilder =
            SubmittedCallbackResponse.builder();

        responseBuilder.confirmationBody(
                CaseDataToTextGenerator.getTextFor(
                    confirmationTextGenerators.stream(),
                    () -> getDefaultConfirmationText(caseData),
                    caseData
                ))
            .confirmationHeader(
                CaseDataToTextGenerator.getTextFor(
                    confirmationHeaderGenerators.stream(),
                    () -> getDefaultConfirmationHeader(caseData),
                    caseData
                ));

        return responseBuilder.build();
    }

    private String getDefaultConfirmationText(CaseData caseData) {
        if (caseData.hasApplicantProceededWithClaim()) {
            return "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                + "We'll review the case and contact you about what to do next.<br>"
                + format(
                "%n%n<a href=\"%s\" target=\"_blank\">View Directions questionnaire</a>",
                format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
            );
        } else {
            return "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                + "You've decided not to proceed and the case will end.<br>"
                + format(
                "%n%n<a href=\"%s\" target=\"_blank\">View Directions questionnaire</a>",
                format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
            );
        }
    }

    private String getDefaultConfirmationHeader(CaseData caseData) {
        String claimNumber = caseData.getLegacyCaseReference();
        if (caseData.hasApplicantProceededWithClaim() && !caseData.hasClaimantAgreedToFreeMediation()) {
            return format(
                "# You have decided to proceed with the claim%n## Claim number: %s",
                claimNumber
            );
        } else if (caseData.hasClaimantAgreedToFreeMediation()) {
            return format(
                "# You have rejected their response %n## Your Claim Number : %s",
                caseData.getLegacyCaseReference()
            );
        } else {
            return format(
                "# You have decided not to proceed with the claim%n## Claim number: %s",
                claimNumber
            );
        }
    }

    private String setUpPayDateToString(CaseData caseData) {
        if (caseData.getRespondToClaimAdmitPartLRspec() != null
            && caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid() != null) {
            return caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid()
                .format(DateTimeFormatter.ofPattern(datePattern, Locale.ENGLISH));
        }
        if (caseData.getRespondToAdmittedClaim() != null
            && caseData.getRespondToAdmittedClaim().getWhenWasThisAmountPaid() != null) {
            return caseData.getRespondToAdmittedClaim().getWhenWasThisAmountPaid()
                .format(DateTimeFormatter.ofPattern(datePattern, Locale.ENGLISH));
        }
        if (caseData.getRespondent1ResponseDate() != null) {
            return deadlineCalculatorService.calculateExtendedDeadline(
                caseData.getRespondent1ResponseDate().toLocalDate(),
                RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY)
                .format(DateTimeFormatter.ofPattern(datePattern, Locale.ENGLISH));
        }
        return null;
    }

    private CallbackResponse validatePaymentDate(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>(1);

        if (checkPastDateValidation(
            caseData.getApplicant1RequestedPaymentDateForDefendantSpec().getPaymentSetDate())) {
            errors.add("Enter a date that is today or in the future");
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse getPaymentDate(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        //Set the hint date for repayment to be 30 days in the future
        String formattedDeadline = formatLocalDateTime(LocalDateTime.now().plusDays(30), DATE);
        caseDataBuilder.currentDateboxDefendantSpec(formattedDeadline);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse suggestInstalmentsValidation(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        //Check repayment amount requested is less than the overall claim amount
        BigDecimal totalClaimAmount = caseData.getTotalClaimAmount();
        BigDecimal regularRepaymentAmountPennies = caseData.getApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec();
        BigDecimal regularRepaymentAmountPounds = MonetaryConversions.penniesToPounds(regularRepaymentAmountPennies);

        if (regularRepaymentAmountPounds.compareTo(ZERO) <= 0) {
            errors.add("Enter an amount of £1 or more");
        }

        if (regularRepaymentAmountPounds.compareTo(totalClaimAmount.subtract(BigDecimal.ONE)) > 0) {
            errors.add("Enter a valid amount for equal instalments");
        }

        LocalDate eligibleDate;
        formatLocalDate(eligibleDate = LocalDate.now().plusDays(30), DATE);
        if (caseData.getApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec().isBefore(eligibleDate.plusDays(
            1))) {
            errors.add("Selected date must be after " + formatLocalDate(eligibleDate, DATE));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private boolean checkPastDateValidation(LocalDate localDate) {
        return localDate != null && localDate.isBefore(LocalDate.now());
    }

    private boolean isDefendantFullAdmitPayImmediately(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && (FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()));
    }

    private CallbackResponse validateAmountPaid(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = judgementService.validateAmountPaid(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.isEmpty() ? caseData.toMap(objectMapper) : null)
            .build();
    }

    private CallbackResponse buildJudgmentAmountSummaryDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        updatedCaseData.ccjPaymentDetails(judgementService.buildJudgmentAmountSummaryDetails(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private boolean isFlightDelayClaim(CaseData caseData) {
        return (featureToggleService.isSdoR2Enabled() && caseData.getIsFlightDelayClaim() != null
            && caseData.getIsFlightDelayClaim().equals(YES));
    }

    private boolean isFlightDelayClaimAndUpdateCaseLocation(CaseData caseData) {
        //Update the Case Management Location when the Airline  is not Other
        return (isFlightDelayClaim(caseData) && caseData.getFlightDelayDetails() != null
            && caseData.getAllocatedTrack() == AllocatedTrack.SMALL_CLAIM
            && caseData.getFlightDelayDetails().getAirlineList()
            .getValue().getCode().equals("OTHER") == false);
    }

    private boolean isFlightDelayClaimAndUpdateCaseLocationWithClaimantCourt(CaseData caseData) {
        //Update the Case Management Location with Claimant preferred court when Airlines is Other
        return (isFlightDelayClaim(caseData) && caseData.getFlightDelayDetails() != null
            && caseData.getFlightDelayDetails().getAirlineList()
                .getValue().getCode().equals("OTHER"));
    }
}
