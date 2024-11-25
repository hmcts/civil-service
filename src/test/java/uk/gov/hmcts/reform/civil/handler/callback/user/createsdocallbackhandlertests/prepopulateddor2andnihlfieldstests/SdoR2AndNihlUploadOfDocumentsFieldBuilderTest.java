package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlUploadOfDocumentsFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2UploadOfDocuments;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlUploadOfDocumentsFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlUploadOfDocumentsFieldBuilder sdoR2AndNihlUploadOfDocumentsFieldBuilder;

    @Test
    void shouldBuildSdoR2UploadOfDocuments() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlUploadOfDocumentsFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2UploadOfDocuments uploadOfDocuments = caseData.getSdoR2UploadOfDocuments();

        assertEquals(SdoR2UiConstantFastTrack.UPLOAD_OF_DOCUMENTS, uploadOfDocuments.getSdoUploadOfDocumentsTxt());
    }
}