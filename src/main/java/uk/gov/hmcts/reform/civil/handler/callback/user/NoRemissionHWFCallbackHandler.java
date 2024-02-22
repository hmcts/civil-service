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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.citizen.HWFFeePaymentOutcomeService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;

@Service
@RequiredArgsConstructor
public class NoRemissionHWFCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NO_REMISSION_HWF);
    private final ObjectMapper objectMapper;
    private final HWFFeePaymentOutcomeService hwfFeePaymentOutcomeService;
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT),
        this::noRemissionHWF,
        callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse noRemissionHWF(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData = hwfFeePaymentOutcomeService.updateOutstandingFee(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
