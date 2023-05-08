package uk.gov.hmcts.reform.civil.handler.callback.user.strategy;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;

@Component
@AllArgsConstructor
public class UploadTranslatedDocumentStrategyFactory {
    private UploadTranslatedDocumentDefaultStrategy uploadTranslatedDocumentDefaultStrategy;
    private UploadTranslatedDocumentV1Strategy uploadTranslatedDocumentV1Strategy;

    public UploadTranslatedDocumentStrategy getUploadTranslatedDocumentStrategy(CallbackVersion version) {
        if(CallbackVersion.V_1.equals(version)){
            return uploadTranslatedDocumentV1Strategy;
        }
      return uploadTranslatedDocumentDefaultStrategy;
    }
}
