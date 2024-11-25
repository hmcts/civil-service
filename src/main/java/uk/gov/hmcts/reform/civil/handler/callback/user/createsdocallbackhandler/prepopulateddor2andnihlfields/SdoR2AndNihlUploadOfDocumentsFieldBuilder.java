package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2UploadOfDocuments;

@Component
public class SdoR2AndNihlUploadOfDocumentsFieldBuilder implements SdoR2AndNihlCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2UploadOfDocuments(SdoR2UploadOfDocuments.builder()
                .sdoUploadOfDocumentsTxt(SdoR2UiConstantFastTrack.UPLOAD_OF_DOCUMENTS)
                .build());
    }
}
