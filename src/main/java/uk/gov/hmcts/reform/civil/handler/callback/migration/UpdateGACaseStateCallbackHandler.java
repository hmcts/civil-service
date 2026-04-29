package uk.gov.hmcts.reform.civil.handler.callback.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateGACaseStateCallbackHandler extends CallbackHandler  implements GeneralApplicationCallbackHandler {

    private final ObjectMapper objectMapper;
    private static final List<CaseEvent> EVENTS = Collections.singletonList(UPDATE_CASE_DATA);

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_SUBMIT), this::updateNextState)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    private CallbackResponse updateNextState(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        String nextState = caseData.getNextState();

        caseData.setNextState(null);

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder = AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper));

        if (nextState != null) {
            responseBuilder.state(nextState);
        }

        return responseBuilder.build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
