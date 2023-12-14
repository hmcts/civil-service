package uk.gov.hmcts.reform.civil.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CaseWorkerDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaseWorkerDocumentServiceTest {

    private static final String FILE_NAME_1 = "Test file 1";
    private CaseWorkerDocumentService caseWorkerDocumentService;

    @BeforeEach
    void setup() {
        caseWorkerDocumentService = new CaseWorkerDocumentService();
    }

    @Test
    void shouldAddCaseDocumentToSystemGeneratedDocuments() {
        //Given
        CaseDocument caseDocument = CaseDocument.builder().documentName(FILE_NAME_1).build();
        CaseData caseData = CaseData.builder().systemGeneratedCaseDocuments(new ArrayList<>())
            .build();
        //When
        List<Element<CaseDocument>> result = caseWorkerDocumentService.getCaseWorkerDocumentsWithAddedDocument(
            caseDocument,
            caseData
        );
        //Then
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getValue().getDocumentName()).isEqualTo(FILE_NAME_1);
    }
}
