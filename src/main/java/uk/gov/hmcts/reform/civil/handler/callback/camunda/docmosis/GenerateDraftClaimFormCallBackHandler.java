package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DRAFT_FORM;

@Service
@RequiredArgsConstructor
public class GenerateDraftClaimFormCallBackHandler extends CallbackHandler {

    private static List<CaseEvent> EVENTS = List.of(GENERATE_DRAFT_FORM);


    @Override
    protected Map<String, Callback> callbacks() {
        return null;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

}
