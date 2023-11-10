package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    UploadTranslatedDocumentDefaultStrategy.class,
    JacksonAutoConfiguration.class,
})
class UploadTranslatedDocumentDefaultStrategyTest {

    private static final String FILE_NAME_1 = "Some file 1";
    private static final String FILE_NAME_2 = "Some file 2";

    @Autowired
    private UploadTranslatedDocumentDefaultStrategy uploadTranslatedDocumentDefaultStrategy;

    @MockBean
    private SystemGeneratedDocumentService systemGeneratedDocumentService;

    @Test
    void shouldReturnDocumentListWithTranslatedDocument() {
        //Given
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(DEFENDANT_RESPONSE)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();
        TranslatedDocument translatedDocument2 = TranslatedDocument
            .builder()
            .documentType(DEFENDANT_RESPONSE)
            .file(Document.builder().documentFileName(FILE_NAME_2).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1),
            element(translatedDocument2)
        );

        CaseData caseData = CaseDataBuilder
            .builder()
            .atStatePendingClaimIssued()
            .build()
            .builder()
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        List<Element<CaseDocument>> documents = List.of(
            element(CaseDocument.builder().documentName(FILE_NAME_1).build()),
            element(CaseDocument.builder().documentName(FILE_NAME_2).build()));
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(any(), any(CallbackParams.class))).willReturn(documents);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData()).extracting("systemGeneratedCaseDocuments")
            .asList()
            .extracting("value")
            .extracting("documentName")
            .isNotNull();

    }

    @Test
    void shouldReturnExistingSystemGeneratedDocumentListWhenNothingReturnedFromService() {
        //Given
        CaseData caseData = CaseDataBuilder
            .builder()
            .atStatePendingClaimIssued()
            .build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData()).extracting("systemGeneratedCaseDocuments")
            .isNotNull();
    }
}
