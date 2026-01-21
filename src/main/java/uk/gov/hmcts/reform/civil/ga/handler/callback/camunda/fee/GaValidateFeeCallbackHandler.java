package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.fee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.VALIDATE_FEE_GASPEC;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaValidateFeeCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(VALIDATE_FEE_GASPEC);
    private static final String TASK_ID = "GeneralApplicationValidateFee";

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::validateFee
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateFee(CallbackParams callbackParams) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(new ArrayList<String>())
            .build();
    }
}
