package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;

@Service
@RequiredArgsConstructor
public class RespondToClaimSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DEFENDANT_RESPONSE_SPEC);

    private final RespondToClaimSpecValidationUtils validationUtils;
    private final RespondToClaimSpecResponseTypeHandlerResponseTypes responseTypeHandlerResponseTypes;
    private final RespondToClaimSpecResponseTypeHandlerClaims responseTypeHandlerClaims;
    private final RespondToClaimSpecCaseDataHandlerRespondentCopy caseDataHandlerRespondentCopy;
    private final RespondToClaimSpecCaseDataHandlerSolicitorDetails caseDataHandlerSolicitorDetails;
    private final RespondToClaimSpecCaseDataHandlerApplicantResponseDeadline caseDataHandlerApplicantResponseDeadline;
    private final RespondToClaimSpecConfirmationBuilder confirmationBuilder;

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
        return caseDataHandlerRespondentCopy.populateRespondent1Copy(callbackParams);
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
        return caseDataHandlerRespondentCopy.resetStatementOfTruth(callbackParams);
    }

    private CallbackResponse validateRespondentPaymentDate(CallbackParams callbackParams) {
        return validationUtils.validateRespondentPaymentDate(callbackParams);
    }

    private CallbackResponse validateCorrespondenceApplicantAddress(CallbackParams callbackParams) {
        return validationUtils.validateCorrespondenceApplicantAddress(callbackParams);
    }

    private CallbackResponse determineLoggedInSolicitor(CallbackParams callbackParams) {
        return caseDataHandlerSolicitorDetails.determineLoggedInSolicitor(callbackParams);
    }

    private CallbackResponse handleDefendAllClaim(CallbackParams callbackParams) {
        return responseTypeHandlerClaims.handleDefendAllClaim(callbackParams);
    }

    private CallbackResponse handleRespondentResponseTypeForSpec(CallbackParams callbackParams) {
        return responseTypeHandlerResponseTypes.handleRespondentResponseTypeForSpec(callbackParams);
    }

    private CallbackResponse handleAdmitPartOfClaim(CallbackParams callbackParams) {
        return responseTypeHandlerClaims.handleAdmitPartOfClaim(callbackParams);
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
        return caseDataHandlerApplicantResponseDeadline.setApplicantResponseDeadline(callbackParams);
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return confirmationBuilder.buildConfirmation(callbackParams);
    }
}
