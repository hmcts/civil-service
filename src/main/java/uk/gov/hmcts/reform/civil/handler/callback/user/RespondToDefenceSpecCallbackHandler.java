package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask.AboutToSubmitRespondToDefenceTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask.BuildConfirmationTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask.PopulateCaseDataTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.citizenui.RespondentMediationService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.MediationUnavailableDatesUtils.checkUnavailable;

@Service
@RequiredArgsConstructor
@Slf4j
public class RespondToDefenceSpecCallbackHandler extends CallbackHandler
    implements ExpertsValidator, WitnessesValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CLAIMANT_RESPONSE_SPEC);
    public static final String DOWNLOAD_URL_CLAIM_DOCUMENTS = "/cases/case-details/%s#Claim documents";
    public static final String PARTIAL_PAYMENT_OFFLINE = "This feature is currently not available, please see guidance below";
    private final ObjectMapper objectMapper;
    private final UnavailableDateValidator unavailableDateValidator;
    private final JudgementService judgementService;
    private final RespondentMediationService respondentMediationService;
    private final AboutToSubmitRespondToDefenceTask aboutToSubmitRespondToDefenceTask;
    private final PopulateCaseDataTask populateCaseDataTask;
    private final BuildConfirmationTask buildConfirmationTask;

    @Value("${court-location.specified-claim.epimms-id}") String cnbcEpimsId;

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
            .put(callbackKey(MID, "validate-mediation-unavailable-dates"), this::validateMediationUnavailableDates)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit)
            .put(callbackKey(ABOUT_TO_START), this::populateCaseData)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .putAll(version1Callbacks())
            .putAll(version2Callbacks())
            .build();
    }

    private Map<String, Callback> version1Callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(V_1, MID, "set-applicant-route-flags"), this::setApplicantRouteFlags)
            .put(callbackKey(V_1, MID, "validate-respondent-payment-date"), this::validatePaymentDate)
            .put(callbackKey(V_1, MID, "get-payment-date"), this::getPaymentDate)
            .put(callbackKey(V_1, MID, "validate-suggest-instalments"), this::suggestInstalmentsValidation)
            .put(callbackKey(V_1, MID, "validate-amount-paid"), this::validateAmountPaid)
            .put(callbackKey(V_1, MID, "set-up-ccj-amount-summary"), this::buildJudgmentAmountSummaryDetails)
            .put(callbackKey(V_1, MID, "set-mediation-show-tag"), this::setMediationShowTag)
            .build();
    }

    private Map<String, Callback> version2Callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(V_2, MID, "set-applicant-route-flags"), this::setApplicantRouteFlags)
            .put(callbackKey(V_2, ABOUT_TO_SUBMIT), this::aboutToSubmit)
            .put(callbackKey(V_2, ABOUT_TO_START), this::populateCaseData)
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
        if (NO.equals(caseData.getApplicant1AcceptAdmitAmountPaidSpec())
            || NO.equals(caseData.getApplicant1PartAdmitConfirmAmountPaidSpec())
            || NO.equals(caseData.getApplicant1PartAdmitIntentionToSettleClaimSpec())) {
            caseDataBuilder.applicant1ProceedWithClaim(YES);
        }
    }

    private CallbackResponse validateMediationUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        if ((caseData.getApp1MediationAvailability() != null
            && YES.equals(caseData.getApp1MediationAvailability().getIsMediationUnavailablityExists()))) {
            checkUnavailable(errors, caseData.getApp1MediationAvailability().getUnavailableDatesForMediation());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
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
        setApplicant1ProceedFlagToYes(caseData, caseDataBuilder);
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
            && caseData.getApplicant1ProceedWithClaim() == YES || YES == caseData.getApplicant1ProceedWithClaimSpec2v1())
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
        return aboutToSubmitRespondToDefenceTask.execute(callbackParams);
    }

    private CallbackResponse populateCaseData(CallbackParams callbackParams) {
        return populateCaseDataTask.execute(callbackParams);
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return buildConfirmationTask.execute(callbackParams);
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

        LocalDate eligibleDate = LocalDate.now().plusDays(30);
        formatLocalDate(eligibleDate, DATE);
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
        if (caseData.getCcjPaymentDetails() != null
            && YES.equals(caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeOption())) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(PARTIAL_PAYMENT_OFFLINE))
                .build();
        }
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

        if (judgementService.isLrFullAdmitRepaymentPlan(caseData)) {
            updatedCaseData.ccjJudgmentAmountShowInterest(YES);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }
}
