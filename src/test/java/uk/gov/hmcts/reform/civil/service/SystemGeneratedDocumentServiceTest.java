package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemGeneratedDocumentServiceTest {

    private static final String FILE_NAME = "Important file";
    private SystemGeneratedDocumentService systemGeneratedDocumentService = new SystemGeneratedDocumentService();

    @Test
    void shouldAddDocumentToSystemGeneratedDocuments() {
        //Given
        Document document = Document.builder().documentFileName(FILE_NAME).build();
        CaseData caseData = CaseData.builder().systemGeneratedCaseDocuments(new ArrayList<>())
            .build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        //When
        List<Element<CaseDocument>> result = systemGeneratedDocumentService
            .getSystemGeneratedDocumentsWithAddedDocument(
                document,
                DocumentType.DEFENDANT_DEFENCE,
                callbackParams
            );
        //Then
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getValue().getDocumentName()).isEqualTo(FILE_NAME);
    }
}
