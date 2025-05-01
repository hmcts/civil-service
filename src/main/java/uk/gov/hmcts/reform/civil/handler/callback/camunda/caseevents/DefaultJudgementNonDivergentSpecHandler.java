package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC;

@Service
@RequiredArgsConstructor
public class DefaultJudgementNonDivergentSpecHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC);
    public static final String TASK_ID = "GenerateDJFormSpec";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
