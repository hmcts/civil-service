package uk.gov.hmcts.reform.civil.handler.callback.user.strategy;

import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;

public interface UploadTranslatedDocumentStrategy {
    CallbackResponse uploadDocument(CallbackParams callbackParams);
}
