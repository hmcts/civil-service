package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CANCEL_UNISSUED_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;

@Service
@RequiredArgsConstructor
public class CancelUnissuedClaimCallbackHandler extends CallbackHandler {

    static final String EVENT_NOT_ALLOWED = "Event Not Allowed";
    static final String REASON_TOO_LONG = "Explain why you are cancelling the claim must be 200 characters or less";
    static final String REASON_INVALID_CHARACTERS = "Enter alphanumerical characters or standard punctuation marks only";
    static final String CONFIRMATION_HEADER = "# This claim has been cancelled";
    static final String CONFIRMATION_BODY = "### What happens next\n\n"
        + "No further action can be taken on this case. You can start a new claim by going to your account.";

    private static final int REASON_MAX_LENGTH = 200;
    // Same allowed characters as the citizen UI cancellation reason
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile(
        "^[A-Za-z0-9 \\t\\r\\n.,!?'\"()&:;@#/$%+=\\u00A3\\u2018\\u2019\\u201C\\u201D\\u2013\\u2014\\u2026-]*$");

    private final ObjectMapper objectMapper;

    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_START), this::aboutToStartValidation,
        callbackKey(MID, "validate-cancel-reason"), this::validateCancelReason,
        callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
        callbackKey(SUBMITTED), this::submitted);

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(CANCEL_UNISSUED_CLAIM);
    }

    private CallbackResponse aboutToStartValidation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isEligible = CaseState.PENDING_CASE_ISSUED == caseData.getCcdState()
            && (isLipClaimantEligible(caseData) || isLrClaimantEligible(caseData));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(isEligible ? Collections.emptyList() : List.of(EVENT_NOT_ALLOWED))
            .build();
    }

    private boolean isLipClaimantEligible(CaseData caseData) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isApplicantLipOneVOne();
    }

    private boolean isLrClaimantEligible(CaseData caseData) {
        return !caseData.isApplicantLiP() && isOneVOne(caseData);
    }

    private CallbackResponse validateCancelReason(CallbackParams callbackParams) {
        String reason = callbackParams.getCaseData().getCancelUnissuedClaimReason();
        List<String> errors = new ArrayList<>();
        if (reason != null) {
            if (reason.length() > REASON_MAX_LENGTH) {
                errors.add(REASON_TOO_LONG);
            }
            if (!ALLOWED_CHARACTERS.matcher(reason).matches()) {
                errors.add(REASON_INVALID_CHARACTERS);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData.setPreviousCCDState(caseData.getCcdState());
        caseData.setCancelUnissuedClaimDate(LocalDate.now());
        caseData.setBusinessProcess(BusinessProcess.ready(CANCEL_UNISSUED_CLAIM));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitted(CallbackParams callbackParams) {
        if (callbackParams.getCaseData().isApplicantLiP()) {
            return emptySubmittedCallbackResponse(callbackParams);
        }
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody(CONFIRMATION_BODY)
            .build();
    }
}
