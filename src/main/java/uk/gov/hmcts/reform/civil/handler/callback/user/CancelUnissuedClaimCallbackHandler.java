package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
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
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CANCEL_UNISSUED_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Service
@RequiredArgsConstructor
public class CancelUnissuedClaimCallbackHandler extends CallbackHandler {

    static final String EVENT_NOT_ALLOWED = "Event Not Allowed";

    private final ObjectMapper objectMapper;

    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_START), this::aboutToStartValidation,
        callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
        callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse);

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
        boolean isEligible = SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.isApplicantLipOneVOne()
            && CaseState.PENDING_CASE_ISSUED == caseData.getCcdState();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(isEligible ? Collections.emptyList() : List.of(EVENT_NOT_ALLOWED))
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
}
