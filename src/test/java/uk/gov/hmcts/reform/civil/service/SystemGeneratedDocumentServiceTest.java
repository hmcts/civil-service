package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

class SystemGeneratedDocumentServiceTest {

    private static final String FILE_NAME_1 = "Some file 1";

    private SystemGeneratedDocumentService systemGeneratedDocumentService = new SystemGeneratedDocumentService();

    @Test
    void shouldAddDocumentToSystemGeneratedDocuments() {
        //Given
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(DEFENDANT_RESPONSE)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseData caseData = CaseData.builder().systemGeneratedCaseDocuments(new ArrayList<>())
            .build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        //When
        List<Element<CaseDocument>> result = systemGeneratedDocumentService
            .getSystemGeneratedDocumentsWithAddedDocument(translatedDocument, callbackParams);

        //Then
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getValue().getDocumentName()).isEqualTo(FILE_NAME_1);
    }

    @Test
    void shouldAddCaseDocumentToSystemGeneratedDocuments() {
        //Given
        CaseDocument caseDocument = CaseDocument.builder().documentName(FILE_NAME_1).build();
        CaseData caseData = CaseData.builder().systemGeneratedCaseDocuments(new ArrayList<>())
            .build();
        //When
        List<Element<CaseDocument>> result = systemGeneratedDocumentService
            .getSystemGeneratedDocumentsWithAddedDocument(
                caseDocument,
                caseData
            );
        //Then
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getValue().getDocumentName()).isEqualTo(FILE_NAME_1);
    }
}
