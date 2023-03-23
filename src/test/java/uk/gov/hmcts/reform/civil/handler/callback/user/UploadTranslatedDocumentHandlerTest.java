package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    UploadTranslatedDocumentHandler.class,
    JacksonAutoConfiguration.class,
})
class UploadTranslatedDocumentHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private UploadTranslatedDocumentHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldUploadTranslatedDocumentSuccessfully() {
            //Given
            CaseData caseData = CaseDataBuilder
                .builder()
                .atStatePendingClaimIssued()
                .build()
                .builder()
                .caseDataLiP(CaseDataLiP
                                 .builder()
                                 .translatedDocument(TranslatedDocument
                                                         .builder()
                                                         .documentType("DEFENDANT_RESPONSE")
                                                         .file(Document.builder().build())
                                                         .build())
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("translatedDocument").isNotNull();
        }
    }

}
