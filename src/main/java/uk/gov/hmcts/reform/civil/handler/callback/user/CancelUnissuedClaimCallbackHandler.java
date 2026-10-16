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
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    static final String CONFIRMATION_HEADER = "# This claim has been cancelled";
    static final String CONFIRMATION_BODY = "### What happens next\n\n"
        + "No further action can be taken on this case. You can start a new claim by going to your account.";

    // EXUI hides empty fields on Check your answers; a single space keeps the row and its Change link
    private static final String BLANK_REASON_PLACEHOLDER = " ";

    private final ObjectMapper objectMapper;

    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_START), this::aboutToStartValidation,
        callbackKey(MID, "populate-cancel-reason"), this::populateBlankReason,
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

    // Length and allowed characters are validated by CCD (field Max / RegularExpression)
    private CallbackResponse populateBlankReason(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String reason = caseData.getCancelUnissuedClaimReason();
        if (reason == null || reason.isBlank()) {
            caseData.setCancelUnissuedClaimReason(BLANK_REASON_PLACEHOLDER);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!caseData.isApplicantLiP()) {
            String reason = caseData.getCancelUnissuedClaimReason();
            caseData.setCancelUnissuedClaimReason(reason == null || reason.isBlank() ? null : reason.strip());
        }
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
