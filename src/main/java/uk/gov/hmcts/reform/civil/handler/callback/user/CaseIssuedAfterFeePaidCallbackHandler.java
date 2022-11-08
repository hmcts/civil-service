package uk.gov.hmcts.reform.civil.handler.callback.user;

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


import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CASE_ISSUED_AFTER_FEE_PAID;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseIssuedAfterFeePaidCallbackHandler extends CallbackHandler {

    // This should be created by the handler/service that receives payment info via our endpoint.
    private static final List<CaseEvent> EVENTS = singletonList(CASE_ISSUED_AFTER_FEE_PAID);

    private String state = CASE_ISSUED.toString();

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::changeApplicationState
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse changeApplicationState(CallbackParams callbackParams) {
        Long caseId = callbackParams.getCaseData().getCcdCaseReference();
        CaseData caseData = callbackParams.getCaseData();
        log.info("Changing state to {} for caseId: {}", state, caseId);
        return AboutToStartOrSubmitCallbackResponse.builder()
                .state(state)
                .build();
    }

}
