package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;

@Component
@AllArgsConstructor
public class UploadTranslatedDocumentStrategyFactory {

    private UploadTranslatedDocumentDefaultStrategy uploadTranslatedDocumentDefaultStrategy;
    private UploadTranslatedDocumentV1Strategy uploadTranslatedDocumentV1Strategy;

    public UploadTranslatedDocumentStrategy getUploadTranslatedDocumentStrategy(CallbackVersion version) {
        if (version == null) {
            return uploadTranslatedDocumentDefaultStrategy;
        }
        return uploadTranslatedDocumentV1Strategy;
    }
}
