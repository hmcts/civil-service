package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments.UploadTranslatedDocumentDefaultStrategy;
import uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments.UploadTranslatedDocumentStrategyFactory;
import uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments.UploadTranslatedDocumentV1Strategy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    UploadTranslatedDocumentHandler.class,
    UploadTranslatedDocumentStrategyFactory.class,
    UploadTranslatedDocumentDefaultStrategy.class,
    UploadTranslatedDocumentV1Strategy.class,
    SystemGeneratedDocumentService.class,
    JacksonAutoConfiguration.class,
})
class UploadTranslatedDocumentHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private UploadTranslatedDocumentHandler handler;

    @Autowired
    private UploadTranslatedDocumentStrategyFactory uploadTranslatedDocumentStrategyFactory;

    @Autowired
    private UploadTranslatedDocumentDefaultStrategy uploadTranslatedDocumentDefaultStrategy;

    @Autowired
    private SystemGeneratedDocumentService systemGeneratedDocumentService;

    @Autowired
    private ObjectMapper objectMapper;
    private static final String FILE_NAME_1 = "Some file 1";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldUploadTranslatedDocumentSuccessfully() {
            //Given
            TranslatedDocument translatedDocument1 = TranslatedDocument
                .builder()
                .documentType(DEFENDANT_RESPONSE)
                .file(Document.builder().documentFileName(FILE_NAME_1).build())
                .build();
            List<Element<TranslatedDocument>> translatedDocument = List.of(
                element(translatedDocument1)
            );

            CaseData caseData = CaseDataBuilder
                .builder()
                .atStatePendingClaimIssued()
                .build()
                .builder()
                .systemGeneratedCaseDocuments(new ArrayList<>())
                .caseDataLiP(CaseDataLiP
                                 .builder()
                                 .translatedDocuments(translatedDocument)
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("translatedDocument").isNull();
        }
    }

}
