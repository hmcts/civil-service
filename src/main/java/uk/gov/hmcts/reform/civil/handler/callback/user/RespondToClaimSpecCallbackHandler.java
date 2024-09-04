package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.HandleDefendAllClaim;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.PopulateRespondent1Copy;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.DetermineLoggedInSolicitor;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.HandleAdmitPartOfClaim;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecResponseTypeHandlerResponseTypes;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecValidationUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.SetApplicantResponseDeadline;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.CaseDataToTextGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationHeaderSpecGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class RespondToClaimSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DEFENDANT_RESPONSE_SPEC);

    private final RespondToClaimSpecValidationUtils validationUtils;
    private final RespondToClaimSpecResponseTypeHandlerResponseTypes responseTypeHandlerResponseTypes;
    private final HandleAdmitPartOfClaim handleAdmitPartOfClaim;
    private final HandleDefendAllClaim handleDefendAllClaim;
    private final PopulateRespondent1Copy populateRespondent1Copy;
    private final DetermineLoggedInSolicitor determineLoggedInSolicitor;
    private final SetApplicantResponseDeadline setApplicantResponseDeadline;
    private final ObjectMapper objectMapper;
    private final List<RespondToClaimConfirmationTextSpecGenerator> confirmationTextSpecGenerators;
    private final List<RespondToClaimConfirmationHeaderSpecGenerator> confirmationHeaderGenerators;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return ImmutableMap.<String, Callback>builder()
            .put(callbackKey(ABOUT_TO_START), this::populateRespondent1Copy)
            .put(callbackKey(MID, "validate-mediation-unavailable-dates"), this::validateMediationUnavailableDates)
            .put(callbackKey(MID, "confirm-details"), this::validateDateOfBirth)
            .put(callbackKey(MID, "validate-unavailable-dates"), this::validateUnavailableDates)
            .put(callbackKey(MID, "experts"), this::validateRespondentExperts)
            .put(callbackKey(MID, "witnesses"), this::validateRespondentWitnesses)
            .put(callbackKey(MID, "upload"), this::emptyCallbackResponse)
            .put(callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth)
            .put(callbackKey(MID, "validate-payment-date"), this::validateRespondentPaymentDate)
            .put(callbackKey(MID, "specCorrespondenceAddress"), this::validateCorrespondenceApplicantAddress)
            .put(callbackKey(MID, "determineLoggedInSolicitor"), this::determineLoggedInSolicitor)
            .put(callbackKey(MID, "track"), this::handleDefendAllClaim)
            .put(callbackKey(MID, "specHandleResponseType"), this::handleRespondentResponseTypeForSpec)
            .put(callbackKey(MID, "specHandleAdmitPartClaim"), this::handleAdmitPartOfClaim)
            .put(callbackKey(MID, "validate-length-of-unemployment"), this::validateLengthOfUnemployment)
            .put(callbackKey(MID, "validate-repayment-plan"), this::validateDefendant1RepaymentPlan)
            .put(callbackKey(MID, "validate-repayment-plan-2"), this::validateDefendant2RepaymentPlan)
            .put(callbackKey(MID, "set-generic-response-type-flag"), this::setGenericResponseTypeFlag)
            .put(callbackKey(MID, "set-upload-timeline-type-flag"), this::setUploadTimelineTypeFlag)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::setApplicantResponseDeadline)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse populateRespondent1Copy(CallbackParams callbackParams) {
        return populateRespondent1Copy.execute(callbackParams);
    }

    private CallbackResponse validateMediationUnavailableDates(CallbackParams callbackParams) {
        return validationUtils.validateMediationUnavailableDates(callbackParams);
    }

    private CallbackResponse validateDateOfBirth(CallbackParams callbackParams) {
        return validationUtils.validateDateOfBirth(callbackParams);
    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        return validationUtils.validateUnavailableDates(callbackParams);
    }

    private CallbackResponse validateRespondentExperts(CallbackParams callbackParams) {
        return validationUtils.validateRespondentExperts(callbackParams);
    }

    private CallbackResponse validateRespondentWitnesses(CallbackParams callbackParams) {
        return validationUtils.validateRespondentWitnesses(callbackParams);
    }

    private CallbackResponse resetStatementOfTruth(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData updatedCaseData = caseData.toBuilder()
            .uiStatementOfTruth(StatementOfTruth.builder().role("").build())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateRespondentPaymentDate(CallbackParams callbackParams) {
        return validationUtils.validateRespondentPaymentDate(callbackParams);
    }

    private CallbackResponse validateCorrespondenceApplicantAddress(CallbackParams callbackParams) {
        return validationUtils.validateCorrespondenceApplicantAddress(callbackParams);
    }

    private CallbackResponse determineLoggedInSolicitor(CallbackParams callbackParams) {
        return determineLoggedInSolicitor.execute(callbackParams);
    }

    private CallbackResponse handleDefendAllClaim(CallbackParams callbackParams) {
        return handleDefendAllClaim.execute(callbackParams);
    }

    private CallbackResponse handleRespondentResponseTypeForSpec(CallbackParams callbackParams) {
        return responseTypeHandlerResponseTypes.handleRespondentResponseTypeForSpec(callbackParams);
    }

    private CallbackResponse handleAdmitPartOfClaim(CallbackParams callbackParams) {
        return handleAdmitPartOfClaim.execute(callbackParams);
    }

    private CallbackResponse validateLengthOfUnemployment(CallbackParams callbackParams) {
        return validationUtils.validateLengthOfUnemployment(callbackParams);
    }

    private CallbackResponse validateDefendant1RepaymentPlan(CallbackParams callbackParams) {
        return validationUtils.validateDefendant1RepaymentPlan(callbackParams);
    }

    private CallbackResponse validateDefendant2RepaymentPlan(CallbackParams callbackParams) {
        return validationUtils.validateDefendant2RepaymentPlan(callbackParams);
    }

    private CallbackResponse setGenericResponseTypeFlag(CallbackParams callbackParams) {
        return responseTypeHandlerResponseTypes.setGenericResponseTypeFlag(callbackParams);
    }

    private CallbackResponse setUploadTimelineTypeFlag(CallbackParams callbackParams) {
        return responseTypeHandlerResponseTypes.setUploadTimelineTypeFlag(callbackParams);
    }

    private CallbackResponse setApplicantResponseDeadline(CallbackParams callbackParams) {
        return setApplicantResponseDeadline.execute(callbackParams);
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String claimNumber = caseData.getLegacyCaseReference();

        String body = generateConfirmationBody(caseData);
        String header = generateConfirmationHeader(caseData, claimNumber);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build();
    }

    private String generateConfirmationBody(CaseData caseData) {
        return CaseDataToTextGenerator.getTextFor(
            confirmationTextSpecGenerators.stream(),
            () -> getDefaultConfirmationBody(caseData),
            caseData
        );
    }

    private String generateConfirmationHeader(CaseData caseData, String claimNumber) {
        return CaseDataToTextGenerator.getTextFor(
            confirmationHeaderGenerators.stream(),
            () -> format("# You have submitted your response%n## Claim number: %s", claimNumber),
            caseData
        );
    }

    private String getDefaultConfirmationBody(CaseData caseData) {
        LocalDateTime responseDeadline = caseData.getApplicant1ResponseDeadline();
        String nextStepsMessage = getNextStepsMessage(responseDeadline);
        return format(
            "<h2 class=\"govuk-heading-m\">What happens next</h2>%n%n%s%n%n<a href=\"%s\" target=\"_blank\">Download questionnaire (opens in a new tab)</a>",
            nextStepsMessage,
            format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
        );
    }

    private String getNextStepsMessage(LocalDateTime responseDeadline) {
        if (responseDeadline == null) {
            return "After the other solicitor has responded and/or the time for responding has passed the claimant will be notified.";
        } else {
            return format("The claimant has until 4pm on %s to respond to your claim. We will let you know when they respond.",
                          formatLocalDateTime(responseDeadline, DATE));
        }
    }
}
