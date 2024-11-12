package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.sdor2andnihlfields;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2UploadOfDocuments;

@Slf4j
@Component
public class SdoR2NihlUploadOfDocumentsFieldBuilder implements SdoR2AndNihlFieldsCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2UploadOfDocuments(SdoR2UploadOfDocuments.builder()
                .sdoUploadOfDocumentsTxt(SdoR2UiConstantFastTrack.UPLOAD_OF_DOCUMENTS)
                .build());
    }
}
