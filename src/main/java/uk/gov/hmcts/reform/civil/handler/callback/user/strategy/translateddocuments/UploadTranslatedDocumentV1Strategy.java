package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;

@Component
public class UploadTranslatedDocumentV1Strategy implements UploadTranslatedDocumentStrategy {

    @Override
    public CallbackResponse uploadDocument(CallbackParams callbackParams) {
        return null;
    }
}
