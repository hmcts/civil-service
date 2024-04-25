package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC;

@Service
@RequiredArgsConstructor
public class RequestJudgementByAdmissionNonDivergentSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
