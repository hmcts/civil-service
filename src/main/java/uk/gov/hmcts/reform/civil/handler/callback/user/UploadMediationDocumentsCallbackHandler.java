package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_MEDIATION_DOCUMENTS;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadMediationDocumentsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPLOAD_MEDIATION_DOCUMENTS);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
