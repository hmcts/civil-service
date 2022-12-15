package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESET_PIN;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPinCUICallbackHandler extends CallbackHandler {

    private final DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::resetPinExpiryDate,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return List.of(RESET_PIN);
    }

    private CallbackResponse resetPinExpiryDate(CallbackParams callbackParams) {
        CaseData updatedCase = callbackParams.getCaseData().toBuilder()
            .respondent1PinToPostLRspec(defendantPinToPostLRspecService
                                            .resetPinExpiryDate(callbackParams
                                                                    .getCaseData()
                                                                    .getRespondent1PinToPostLRspec()))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCase.toMap(objectMapper))
            .build();
    }
}
