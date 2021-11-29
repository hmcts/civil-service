package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PROCEEDS_IN_HERITAGE_SYSTEM_SPEC;

@Service
@RequiredArgsConstructor
public class ProceedOfflineForSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(PROCEEDS_IN_HERITAGE_SYSTEM_SPEC);
    private static final String TASK_ID = "ProceedOfflineForSpec";

    private final ObjectMapper objectMapper;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::captureTakenOfflineDate);
    }

    private CallbackResponse captureTakenOfflineDate(CallbackParams callbackParams) {
        CaseData caseDataUpdated = callbackParams.getCaseData().toBuilder()
            .takenOfflineDate(LocalDateTime.now())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
