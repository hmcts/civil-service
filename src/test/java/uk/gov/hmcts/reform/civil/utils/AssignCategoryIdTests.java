package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

public class AssignCategoryIdTests {

    private AssignCategoryId assignCategoryId;

    private FeatureToggleService featureToggleService;

    private CaseDocument testCaseDocument = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(SEALED_CLAIM)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    private Document testDocument = Document.builder()
        .documentUrl("testUrl")
        .documentBinaryUrl("testBinUrl")
        .documentFileName("testFileName")
        .documentHash("testDocumentHash")
        .build();

    @BeforeEach
    void setup() {
        featureToggleService = mock(FeatureToggleService.class);
        assignCategoryId = new AssignCategoryId(featureToggleService);
    }

    @Test
    public void shouldNotAssignCategory_whenInvokedAndToggleFalse() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(false);
        List<Element<CaseDocument>> documentList = new ArrayList<>();
        documentList.add(element(testCaseDocument));
        assignCategoryId.assignCategoryIdToDocument(testDocument, "testDocumentID");
        assignCategoryId.assignCategoryIdToCollection(documentList, document -> document.getValue().getDocumentLink(),
                                                 "testDocumentCollectionID");
        assignCategoryId.assignCategoryIdToCaseDocument(testCaseDocument, "testCaseDocumentID");

        assertThat(documentList.get(0).getValue().getDocumentLink().getCategoryID()).isNull();
        assertThat(testDocument.getCategoryID()).isNull();
        assertThat(testCaseDocument.getDocumentLink().getCategoryID()).isNull();
    }

    @Test
    public void shouldAssignCaseDocumentCategoryId_whenInvoked() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        assignCategoryId.assignCategoryIdToCaseDocument(testCaseDocument, "testCaseDocumentID");

        assertThat(testCaseDocument.getDocumentLink().getCategoryID()).isEqualTo("testCaseDocumentID");
    }

    @Test
    public void shouldAssignDocumentCategoryId_whenInvoked() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        assignCategoryId.assignCategoryIdToDocument(testDocument, "testDocumentID");

        assertThat(testDocument.getCategoryID()).isEqualTo("testDocumentID");
    }

    @Test
    public void shouldAssignDocumentIdCollection_whenInvoked() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        List<Element<CaseDocument>> documentList = new ArrayList<>();
        documentList.add(element(testCaseDocument));
        assignCategoryId.assignCategoryIdToCollection(documentList, document -> document.getValue().getDocumentLink(),
                                                 "testDocumentCollectionID");

        assertThat(documentList.get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("testDocumentCollectionID");
    }

    @Test
    public void shouldCopyDocumentWithCategoryId_whenInvoked() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        CaseDocument copy = assignCategoryId.copyCaseDocumentWithCategoryId(testCaseDocument, "testDocumentID");
        assertThat(copy.getDocumentLink().getCategoryID()).isEqualTo("testDocumentID");
    }

    @Test
    public void shouldCopyDocumentsWithCategoryId_whenInvoked() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        List<Element<CaseDocument>> documentList = new ArrayList<>();
        documentList.add(element(testCaseDocument));
        List<Element<CaseDocument>> copyList = assignCategoryId.copyCaseDocumentListWithCategoryId(documentList,
                "testDocumentCollectionID");

        assertThat(copyList.get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("testDocumentCollectionID");
    }

    @Test
    public void shouldNotCopy_whenInvokedAndToggleFalse() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(false);
        List<Element<CaseDocument>> documentList = new ArrayList<>();
        documentList.add(element(testCaseDocument));
        CaseDocument copy = assignCategoryId.copyCaseDocumentWithCategoryId(testCaseDocument, "testDocumentID");
        List<Element<CaseDocument>> copyList = assignCategoryId.copyCaseDocumentListWithCategoryId(documentList,
                "testDocumentCollectionID");

        assertThat(copyList).isNull();
        assertThat(copy).isNull();
    }

}
