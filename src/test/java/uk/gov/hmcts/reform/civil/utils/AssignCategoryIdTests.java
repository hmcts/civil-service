package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsReferredInStatement;
import uk.gov.hmcts.reform.civil.model.mediation.MediationNonAttendanceStatement;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
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

    private MediationNonAttendanceStatement testMediationNonAttDocument = MediationNonAttendanceStatement.builder()
        .yourName("Joe Bloggs")
        .documentDate(LocalDate.of(2023, 10, 7))
        .documentUploadedDatetime(LocalDateTime.of(2024, 01, 10, 12, 13, 12))
        .document(Document.builder()
                      .documentUrl("fake-url")
                      .documentFileName("file-name")
                      .documentBinaryUrl("binary-url")
                      .build())
        .build();

    private MediationDocumentsReferredInStatement testMediationDocRefDocument = MediationDocumentsReferredInStatement.builder()
        .documentType("doc-type")
        .documentDate(LocalDate.of(2023, 10, 7))
        .documentUploadedDatetime(LocalDateTime.of(2024, 01, 10, 12, 13, 12))
        .document(Document.builder()
                      .documentUrl("fake-url")
                      .documentFileName("file-name")
                      .documentBinaryUrl("binary-url")
                      .build())
        .build();

    @BeforeEach
    void setup() {
        featureToggleService = mock(FeatureToggleService.class);
        assignCategoryId = new AssignCategoryId(featureToggleService);
    }

    @Test
    void shouldNotAssignCategory_whenInvokedAndToggleFalse() {
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
    void shouldAssignCaseDocumentCategoryId_whenInvoked() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        assignCategoryId.assignCategoryIdToCaseDocument(testCaseDocument, "testCaseDocumentID");

        assertThat(testCaseDocument.getDocumentLink().getCategoryID()).isEqualTo("testCaseDocumentID");
    }

    @Test
    void shouldAssignDocumentCategoryId_whenInvoked() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        assignCategoryId.assignCategoryIdToDocument(testDocument, "testDocumentID");

        assertThat(testDocument.getCategoryID()).isEqualTo("testDocumentID");
    }

    @Test
    void shouldAssignDocumentIdCollection_whenInvoked() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        List<Element<CaseDocument>> documentList = new ArrayList<>();
        documentList.add(element(testCaseDocument));
        assignCategoryId.assignCategoryIdToCollection(documentList, document -> document.getValue().getDocumentLink(),
                                                 "testDocumentCollectionID");

        assertThat(documentList.get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("testDocumentCollectionID");
    }

    @Test
    void shouldCopyDocumentWithCategoryId_whenInvoked() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        CaseDocument copy = assignCategoryId.copyCaseDocumentWithCategoryId(testCaseDocument, "testDocumentID");
        assertThat(copy.getDocumentLink().getCategoryID()).isEqualTo("testDocumentID");
    }

    @Test
    void shouldCopyDocumentsWithCategoryId_whenInvoked() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        List<Element<CaseDocument>> documentList = new ArrayList<>();
        documentList.add(element(testCaseDocument));
        List<Element<CaseDocument>> copyList = assignCategoryId.copyCaseDocumentListWithCategoryId(documentList,
                "testDocumentCollectionID");

        assertThat(copyList.get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("testDocumentCollectionID");
    }

    @Test
    void shouldNotCopy_whenInvokedAndToggleFalse() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(false);
        List<Element<CaseDocument>> documentList = new ArrayList<>();
        documentList.add(element(testCaseDocument));
        CaseDocument copy = assignCategoryId.copyCaseDocumentWithCategoryId(testCaseDocument, "testDocumentID");
        List<Element<CaseDocument>> copyList = assignCategoryId.copyCaseDocumentListWithCategoryId(documentList,
                "testDocumentCollectionID");

        assertThat(copyList).isNull();
        assertThat(copy).isNull();
    }

    @Test
    void shouldCopyDocumentsWithCategoryIdMediationNonAtt_whenInvoked() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        List<Element<MediationNonAttendanceStatement>> documentList = new ArrayList<>();
        documentList.add(element(testMediationNonAttDocument));
        List<Element<MediationNonAttendanceStatement>> copyList = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationNonAtt(documentList,
                                                                                                   "testDocumentCollectionID");

        assertThat(copyList.get(0).getValue().getDocument().getCategoryID()).isEqualTo("testDocumentCollectionID");
    }

    @Test
    void shouldNotCopyCaseDocumentListWithCategoryIdMediationNonAtt_whenInvokedAndToggleFalse() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(false);
        List<Element<MediationNonAttendanceStatement>> documentList = new ArrayList<>();
        documentList.add(element(testMediationNonAttDocument));
        List<Element<MediationNonAttendanceStatement>> copyList = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationNonAtt(documentList,
                                                                                                   "testDocumentCollectionID");

        assertThat(copyList).isNull();
    }

    @Test
    void shouldCopyCaseDocumentListWithCategoryIdMediationDocRef_whenInvoked() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        List<Element<MediationDocumentsReferredInStatement>> documentList = new ArrayList<>();
        documentList.add(element(testMediationDocRefDocument));
        List<Element<MediationDocumentsReferredInStatement>> copyList = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationDocRef(documentList,
                                                                                                                                     "testDocumentCollectionID");

        assertThat(copyList.get(0).getValue().getDocument().getCategoryID()).isEqualTo("testDocumentCollectionID");
    }

    @Test
    void shouldNotCopyCaseDocumentListWithCategoryIdMediationDocRef_whenInvokedAndToggleFalse() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(false);
        List<Element<MediationDocumentsReferredInStatement>> documentList = new ArrayList<>();
        documentList.add(element(testMediationDocRefDocument));
        List<Element<MediationDocumentsReferredInStatement>> copyList = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationDocRef(documentList,
                                                                                                                                     "testDocumentCollectionID");

        assertThat(copyList).isNull();
    }

}
