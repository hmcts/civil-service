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

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_TRANSLATED_DOCUMENT;

@Service
@RequiredArgsConstructor
public class UploadTranslatedDocumentHandler extends CallbackHandler {

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::uploadDocument);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return List.of(UPLOAD_TRANSLATED_DOCUMENT);
    }

    private CallbackResponse uploadDocument(CallbackParams callbackParams) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackParams.getCaseData().toMap(objectMapper))
            .build();
    }
}
