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
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.CaseDataToTextGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;

import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

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
    private final LocationHelper locationHelper;
    private static final String datePattern = "dd MMMM yyyy";

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
            .put(callbackKey(V_1, MID, "validate-respondent-payment-date"), this::validatePaymentDate)
            .put(callbackKey(V_1, MID, "get-payment-date"), this::getPaymentDate)
            .put(callbackKey(V_1, MID, "validate-suggest-instalments"), this::suggestInstalmentsValidation)
            .put(callbackKey(V_1, MID, "validate-amount-paid"), this::validateAmountPaid)
            .put(callbackKey(V_1, MID, "set-up-ccj-amount-summary"), this::buildJudgmentAmountSummaryDetails)
            .put(callbackKey(ABOUT_TO_SUBMIT), params -> aboutToSubmit(params, false))
            .put(callbackKey(V_1, ABOUT_TO_SUBMIT), params -> aboutToSubmit(params, true))
            .put(callbackKey(ABOUT_TO_START), this::populateCaseData)
            .put(callbackKey(V_1, ABOUT_TO_START), this::populateCaseData)
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
        CaseData updatedCaseData = setApplicant1ProceedFlagToYes(callbackParams.getCaseData());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CaseData setApplicant1ProceedFlagToYes(CaseData caseData) {
        var updatedCaseData = caseData.toBuilder();

        if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))
            && YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
            updatedCaseData.applicant1ProceedWithClaim(YES);
        }
        return updatedCaseData.build();
    }

    private YesOrNo doesPartPaymentRejectedOrItsFullDefenceResponse(CaseData caseData) {
        if (NO.equals(caseData.getApplicant1AcceptAdmitAmountPaidSpec())
            || (caseData.getRespondent1ClaimResponseTypeForSpec().equals(RespondentResponseTypeSpec.FULL_DEFENCE)
            && !(NO.equals(caseData.getApplicant1ProceedWithClaim()))
            && !(NO.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())))) {
            return YES;
        }
        return NO;
    }

    private CaseData setApplicantDefenceResponseDocFlag(CaseData caseData) {
        var updatedCaseData = caseData.toBuilder();
        updatedCaseData.applicantDefenceResponseDocumentAndDQFlag(doesPartPaymentRejectedOrItsFullDefenceResponse(caseData));

        return updatedCaseData.build();
    }

    private CallbackResponse setApplicantRouteFlags(CallbackParams callbackParams) {
        CaseData updatedCaseData = setApplicantDefenceResponseDocFlag(setApplicant1ProceedFlagToYes(callbackParams.getCaseData()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
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
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE_SPEC))
            .applicant1ResponseDate(time.now());

        if (v1) {
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

        if (caseData.getApplicant1ProceedWithClaim() == YES
            || caseData.getApplicant1ProceedWithClaimSpec2v1() == YES) {
            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Applicant1DQ.Applicant1DQBuilder dq = caseData.getApplicant1DQ().toBuilder()
                .applicant1DQStatementOfTruth(statementOfTruth);
            if (V_1.equals(callbackParams.getVersion())
                && featureToggleService.isCourtLocationDynamicListEnabled()) {

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

            if (featureToggleService.isHearingAndListingSDOEnabled()) {
                dq.applicant1DQWitnesses(builder.build().getApplicant1DQWitnessesSmallClaim());
            }

            builder.applicant1DQ(dq.build());
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            builder.uiStatementOfTruth(StatementOfTruth.builder().build());
        }

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(builder.build().toMap(objectMapper));

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        if (v1 && featureToggleService.isSdoEnabled()) {
            if (caseData.getRespondent1ClaimResponseTypeForSpec().equals(RespondentResponseTypeSpec.FULL_DEFENCE)) {
                if ((multiPartyScenario.equals(ONE_V_ONE) || multiPartyScenario.equals(TWO_V_ONE))
                    || multiPartyScenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
                    response.state(CaseState.JUDICIAL_REFERRAL.name());
                } else if (multiPartyScenario.equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                    if (caseData.getRespondent2ClaimResponseTypeForSpec()
                        .equals(RespondentResponseTypeSpec.FULL_DEFENCE)) {
                        response.state(CaseState.JUDICIAL_REFERRAL.name());
                    }
                }
            }
        }

        return response.build();
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
                            .caseLocation(LocationRefDataService.buildCaseLocation(courtLocation))
                            .responseCourtCode(courtLocation.getCourtLocationCode()).build()
                    ).build());
            }
        }
    }

    private CallbackResponse populateCaseData(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        updatedCaseData.respondent1Copy(caseData.getRespondent1())
            .claimantResponseScenarioFlag(getMultiPartyScenario(caseData))
            .caseAccessCategory(CaseCategory.SPEC_CLAIM);

        if (V_1.equals(callbackParams.getVersion()) && featureToggleService.isCourtLocationDynamicListEnabled()) {
            List<LocationRefData> locations = fetchLocationData(callbackParams);
            updatedCaseData.applicant1DQ(
                Applicant1DQ.builder().applicant1DQRequestedCourt(
                    RequestedCourt.builder().responseCourtLocations(
                        courtLocationUtils.getLocationsFromList(locations)).build()
                    ).build());
        }

        if (V_2.equals(callbackParams.getVersion()) && featureToggleService.isPinInPostEnabled()) {
            updatedCaseData.showResponseOneVOneFlag(setUpOneVOneFlow(caseData));
            updatedCaseData.respondent1PaymentDateToStringSpec(setUpPayDateToString(caseData));

            Optional<BigDecimal> howMuchWasPaid = Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                .map(RespondToClaim::getHowMuchWasPaid);

            howMuchWasPaid.ifPresent(howMuchWasPaidValue -> updatedCaseData.partAdmitPaidValuePounds(
                MonetaryConversions.penniesToPounds(howMuchWasPaidValue)));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (featureToggleService.isSdoEnabled() && !AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            caseData.toBuilder().ccdState(CaseState.JUDICIAL_REFERRAL).build();
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
        if (YesOrNo.YES.equals(caseData.getApplicant1ProceedWithClaim())
            || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
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
        if (YesOrNo.YES.equals(caseData.getApplicant1ProceedWithClaim())
            || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
            return format(
                "# You have decided to proceed with the claim%n## Claim number: %s",
                claimNumber
            );
        } else {
            return format(
                "# You have decided not to proceed with the claim%n## Claim number: %s",
                claimNumber
            );
        }
    }

    private ResponseOneVOneShowTag setUpOneVOneFlow(CaseData caseData) {
        if (ONE_V_ONE.equals(getMultiPartyScenario(caseData))) {
            if (caseData.getRespondent1ClaimResponseTypeForSpec() == null) {
                return null;
            }
            switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_DEFENCE:
                    return ResponseOneVOneShowTag.ONE_V_ONE_FULL_DEFENCE;
                case FULL_ADMISSION:
                    return setUpOneVOneFlowForFullAdmit(caseData);
                case PART_ADMISSION:
                    return setUpOneVOneFlowForPartAdmit(caseData);
                case COUNTER_CLAIM:
                    return ResponseOneVOneShowTag.ONE_V_ONE_COUNTER_CLAIM;
                default:
                    return null;
            }
        }
        return null;
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
            return caseData.getRespondent1ResponseDate().plusDays(5)
                .format(DateTimeFormatter.ofPattern(datePattern, Locale.ENGLISH));
        }
        return null;
    }

    private ResponseOneVOneShowTag setUpOneVOneFlowForPartAdmit(CaseData caseData) {
        if (YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            return ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_HAS_PAID;
        }
        switch (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()) {
            case IMMEDIATELY:
                return ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY;
            case BY_SET_DATE:
                return ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_BY_SET_DATE;
            case SUGGESTION_OF_REPAYMENT_PLAN:
                return ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_INSTALMENT;
            default:
                return ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT;
        }
    }

    private ResponseOneVOneShowTag setUpOneVOneFlowForFullAdmit(CaseData caseData) {
        if (YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            return ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_HAS_PAID;
        }
        switch (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()) {
            case IMMEDIATELY:
                return ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_PAY_IMMEDIATELY;
            case BY_SET_DATE:
                return ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_PAY_BY_SET_DATE;
            case SUGGESTION_OF_REPAYMENT_PLAN:
                return ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_PAY_INSTALMENT;
            default:
                return ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT;
        }
    }

    private CallbackResponse validatePaymentDate(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>(1);

        if (checkPastDateValidation(caseData.getApplicant1RequestedPaymentDateForDefendantSpec())) {
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

    private CallbackResponse validateAmountPaid(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        if (isPaidSomeAmountMoreThanClaimAmount(caseData)) {
            errors.add("The amount paid must be less than the full claim amount.");
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse buildJudgmentAmountSummaryDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        BigDecimal claimAmount = caseData.getTotalClaimAmount();
        if (YesOrNo.YES.equals(caseData.getApplicant1AcceptPartAdmitPaymentPlanSpec())) {
            claimAmount = caseData.getRespondToAdmittedClaimOwingAmountPounds();
        }
        BigDecimal claimFee =  MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence());
        BigDecimal paidAmount = (caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeOption() == YesOrNo.YES)
            ? MonetaryConversions.penniesToPounds(caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeAmount()) : ZERO;
        BigDecimal fixedCost = setUpFixedCostAmount(claimAmount, caseData);
        BigDecimal subTotal =  claimAmount.add(claimFee).add(caseData.getTotalInterest()).add(fixedCost);
        BigDecimal finalTotal = subTotal.subtract(paidAmount);

        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
            .ccjJudgmentAmountClaimAmount(claimAmount)
            .ccjJudgmentAmountClaimFee(claimFee)
            .ccjJudgmentSummarySubtotalAmount(subTotal)
            .ccjJudgmentTotalStillOwed(finalTotal)
            .ccjJudgmentAmountInterestToDate(caseData.getTotalInterest())
            .ccjPaymentPaidSomeAmountInPounds(paidAmount)
            .ccjJudgmentFixedCostAmount(fixedCost)
            .build();

        updatedCaseData.ccjPaymentDetails(ccjPaymentDetails);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private BigDecimal setUpFixedCostAmount(BigDecimal claimAmount, CaseData caseData) {
        if (!YES.equals(caseData.getCcjPaymentDetails().getCcjJudgmentFixedCostOption())) {
            return ZERO;
        }
        if (claimAmount.compareTo(BigDecimal.valueOf(25)) < 0) {
            return ZERO;
        } else if (claimAmount.compareTo(BigDecimal.valueOf(5000)) <= 0) {
            return BigDecimal.valueOf(40);
        } else {
            return BigDecimal.valueOf(55);
        }
    }

    private boolean isPaidSomeAmountMoreThanClaimAmount(CaseData caseData) {
        return caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeAmount() != null
            && caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeAmount()
            .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount()))) > 0;
    }
}
