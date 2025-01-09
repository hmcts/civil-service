package uk.gov.hmcts.reform.civil.service.documentremoval;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseNoteType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeep;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeepCollection;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithName;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class DocumentRemovalServiceTest {

    public static final long CASE_ID = 1234567890L;
    private final String firstDate = "2024-01-01T00:00:00.000000";
    private final String secondDate = "2024-01-02T00:00:00.000000";
    private final String thirdDate = "2024-01-03T00:00:00.000000";
    private final String fourthDate = "2024-01-04T00:00:00.000000";
    private DocumentRemovalService documentRemovalService;
    @Mock
    private DocumentManagementService documentManagementService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper
            .builder()
            .addModule(new JavaTimeModule())
            .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
        documentRemovalService = new DocumentRemovalService(objectMapper, documentManagementService);
    }

    private CaseDocument buildCaseDocument(String url, String fileName, String binaryUrl, String uploadTimeStamp) {
        return CaseDocument.builder()
            .documentLink(Document.builder()
                .documentUrl(url)
                .documentFileName(fileName)
                .documentBinaryUrl(binaryUrl)
                .uploadTimestamp(uploadTimeStamp)
                .build())
            .build();
    }

    private Document buildDocument(String url, String fileName, String binaryUrl, String uploadTimeStamp) {
        return
            Document.builder()
                .documentUrl(url)
                .documentFileName(fileName)
                .documentBinaryUrl(binaryUrl)
                .uploadTimestamp(uploadTimeStamp)
                .build();
    }

    @Nested
    class GetCaseDocumentList {

        @Test
        void testGetCaseDocumentsList_EmptyObject() {
            List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .build());
            assertEquals(0, result.size());
        }

        @Test
        void testGetCaseDocumentsList_RootDocument() {
            List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .decisionOnReconsiderationDocument(buildCaseDocument(
                    "https://example.com/123", "Form-C.pdf", "https://example.com/binary", firstDate))
                .build());

            assertEquals(1, result.size());
            assertEquals("https://example.com/123", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentUrl());
            assertEquals("Form-C.pdf", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentFileName());
            assertEquals("https://example.com/binary", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentBinaryUrl());
            assertEquals("123", result.get(0).getValue().getDocumentId());
        }

        @Test
        void testGetCaseDocumentsList_withDuplicateDocument() {
            List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .decisionOnReconsiderationDocument(buildCaseDocument(
                    "https://example.com/123", "Form-C.pdf", "https://example.com/binary", firstDate))
                .requestForReconsiderationDocumentRes(buildCaseDocument(
                    "https://example.com/123", "Form-C.pdf", "https://example.com/binary", firstDate))
                .build());

            assertEquals(1, result.size());
            assertEquals("https://example.com/123", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentUrl());
            assertEquals("Form-C.pdf", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentFileName());
            assertEquals("https://example.com/binary", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentBinaryUrl());
            assertEquals("123", result.get(0).getValue().getDocumentId());
        }

        @Test
        void testGetCaseDocumentsList_NestedObjectWithinArrayWithDocumentUrl() {
            List<Element<CaseDocument>> claimantResponseDocuments = new ArrayList<>();
            claimantResponseDocuments.add(element(buildCaseDocument(
                "https://example1.com/123", "Form-C.pdf", "https://example1.com/binary", null)));

            claimantResponseDocuments.add(element(buildCaseDocument(
                "https://example2.com/456", "Form-D.pdf", "https://example2.com/binary", null)));

            List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .claimantResponseDocuments(claimantResponseDocuments)
                .build());

            assertEquals(2, result.size());

            assertEquals("https://example1.com/123", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentUrl());
            assertEquals("Form-C.pdf", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentFileName());
            assertEquals("https://example1.com/binary", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentBinaryUrl());
            assertEquals("123", result.get(0).getValue().getDocumentId());

            assertEquals("https://example2.com/456", result.get(1).getValue().getCaseDocument().getDocumentLink().getDocumentUrl());
            assertEquals("Form-D.pdf", result.get(1).getValue().getCaseDocument().getDocumentLink().getDocumentFileName());
            assertEquals("https://example2.com/binary", result.get(1).getValue().getCaseDocument().getDocumentLink().getDocumentBinaryUrl());
            assertEquals("456", result.get(1).getValue().getDocumentId());
        }

        @Test
        void testGetCaseDocumentsList_NestedObjectWithDocumentUrl() {
            Document document1 = buildDocument(
                "https://example1.com/123", "Form-A.pdf", "https://example1.com/binary", null);
            DocumentWithName documentWithName1 = DocumentWithName.builder().documentName("testDocument1").document(document1).createdBy("bill bob").build();

            Document document2 = buildDocument(
                "https://example2.com/456", "Form-B.pdf", "https://example2.com/binary", null);
            DocumentWithName documentWithName2 = DocumentWithName.builder().documentName("testDocument2").document(document2).createdBy("bill bob").build();

            Document document3 = buildDocument(
                "https://example3.com/789", "Form-C.pdf", "https://example3.com/binary", null);
            DocumentWithName documentWithName3 = DocumentWithName.builder().documentName("testDocument3").document(document3).createdBy("bill bob").build();

            List<Element<DocumentWithName>> documentsList = wrapElements(documentWithName1, documentWithName2, documentWithName3);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .caseNoteType(CaseNoteType.DOCUMENT_ONLY)
                .documentAndNameToAdd(documentsList)
                .build();

            List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(caseData);

            assertEquals(3, result.size());

            assertEquals("https://example1.com/123", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentUrl());
            assertEquals("Form-A.pdf", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentFileName());
            assertEquals("https://example1.com/binary", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentBinaryUrl());
            assertEquals("123", result.get(0).getValue().getDocumentId());

            assertEquals("https://example2.com/456", result.get(1).getValue().getCaseDocument().getDocumentLink().getDocumentUrl());
            assertEquals("Form-B.pdf", result.get(1).getValue().getCaseDocument().getDocumentLink().getDocumentFileName());
            assertEquals("https://example2.com/binary", result.get(1).getValue().getCaseDocument().getDocumentLink().getDocumentBinaryUrl());
            assertEquals("456", result.get(1).getValue().getDocumentId());

            assertEquals("https://example3.com/789", result.get(2).getValue().getCaseDocument().getDocumentLink().getDocumentUrl());
            assertEquals("Form-C.pdf", result.get(2).getValue().getCaseDocument().getDocumentLink().getDocumentFileName());
            assertEquals("https://example3.com/binary", result.get(2).getValue().getCaseDocument().getDocumentLink().getDocumentBinaryUrl());
            assertEquals("789", result.get(2).getValue().getDocumentId());
        }

        @Test
        void testGetCaseDocumentsList_SortingIsCorrect() {

            Document document1 = buildDocument(
                "https://example3.com/789", "ThirdDoc.pdf", "https://example3.com/binary", thirdDate);
            DocumentWithName documentWithName = DocumentWithName.builder().documentName("testDocument3").document(document1).createdBy("bill bob").build();

            List<Element<DocumentWithName>> documentsList = wrapElements(documentWithName);

            List<Element<CaseDocument>> claimantResponseDocuments = new ArrayList<>();
            claimantResponseDocuments.add(element(buildCaseDocument(
                "https://example1.com/123", "FirstDoc.pdf", "https://example1.com/binary", firstDate)));

            claimantResponseDocuments.add(element(buildCaseDocument(
                "https://example4.com/101112", "FourthDoc.pdf", "https://example4.com/binary", fourthDate)));

            claimantResponseDocuments.add(element(buildCaseDocument(
                "https://example5.com/131415", "NullDoc.pdf", "https://example5.com/binary", null)));

            List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .documentAndName(documentsList)
                .claimantResponseDocuments(claimantResponseDocuments)
                .decisionOnReconsiderationDocument(
                    buildCaseDocument("https://example2.com/456", "SecondDoc.pdf", "https://example2.com/binary", secondDate))
                .build());

            assertEquals(5, result.size());

            assertEquals("https://example4.com/101112", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentUrl());
            assertEquals("FourthDoc.pdf", result.get(0).getValue().getCaseDocument().getDocumentLink().getDocumentFileName());

            assertEquals("https://example3.com/789", result.get(1).getValue().getCaseDocument().getDocumentLink().getDocumentUrl());
            assertEquals("ThirdDoc.pdf", result.get(1).getValue().getCaseDocument().getDocumentLink().getDocumentFileName());

            assertEquals("https://example2.com/456", result.get(2).getValue().getCaseDocument().getDocumentLink().getDocumentUrl());
            assertEquals("SecondDoc.pdf", result.get(2).getValue().getCaseDocument().getDocumentLink().getDocumentFileName());

            assertEquals("https://example1.com/123", result.get(3).getValue().getCaseDocument().getDocumentLink().getDocumentUrl());
            assertEquals("FirstDoc.pdf", result.get(3).getValue().getCaseDocument().getDocumentLink().getDocumentFileName());

            assertEquals("https://example5.com/131415", result.get(4).getValue().getCaseDocument().getDocumentLink().getDocumentUrl());
            assertEquals("NullDoc.pdf", result.get(4).getValue().getCaseDocument().getDocumentLink().getDocumentFileName());
        }
    }

    @Nested
    class RemoveDocuments {
        @Test
        void testRemoveDocuments_NoDocuments() {
            CaseData caseData = CaseData.builder()
                .allPartyNames("Some Name")
                .build();
            CaseData result = documentRemovalService.removeDocuments(caseData, 1L, "Auth");

            assertEquals(caseData.getAllPartyNames(), result.getAllPartyNames());
            assertNull(result.getDocumentToKeepCollection());
        }

        @Test
        void testRemoveDocuments_TopLevelDoc() {
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .decisionOnReconsiderationDocument(buildCaseDocument(
                    "https://example1.com/123", "Decision On Reconsideration Doc.pdf", "https://example1.com/binary", null))
                .respondent1GeneratedResponseDocument(buildCaseDocument(
                    "https://example2.com/456", "Respondent1 Generated Response Doc.pdf", "https://example2.com/binary", null))
                .documentToKeepCollection(List.of(DocumentToKeepCollection.builder()
                    .value(DocumentToKeep.builder()
                        .documentId("456")
                        .caseDocument(buildCaseDocument(
                            "https://example2.com/456", "Respondent1 Generated Response Doc.pdf", "https://example2.com/binary", null))
                        .build())
                    .build()))
                .build();

            CaseData result = documentRemovalService.removeDocuments(caseData, 1L, "Auth");

            assertNull(result.getDecisionOnReconsiderationDocument().getDocumentLink());
            assertNotNull(result.getRespondent1GeneratedResponseDocument());
            assertEquals("Respondent1 Generated Response Doc.pdf", result.getRespondent1GeneratedResponseDocument().getDocumentLink().getDocumentFileName());
            assertNull(result.getDocumentToKeepCollection());
        }

        @Test
        void testRemoveDocuments_KeepAllDocuments() {

            List<Element<CaseDocument>> claimantResponseDocuments = new ArrayList<>();
            claimantResponseDocuments.add(element(buildCaseDocument(
                "https://example1.com/123", "Form-C.pdf", "https://example1.com/binary", null)));

            claimantResponseDocuments.add(element(buildCaseDocument(
                "https://example2.com/456", "Form-D.pdf", "https://example2.com/binary", null)));
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .claimantResponseDocuments(claimantResponseDocuments)
                .documentToKeepCollection(List.of(DocumentToKeepCollection.builder()
                        .value(DocumentToKeep.builder()
                            .documentId("123")
                            .caseDocument(CaseDocument.builder()
                                .documentLink(buildDocument("https://example1.com/123", "Form-C.pdf", "https://example1.com/binary", null))
                                .build())
                            .build())
                        .build(),
                    DocumentToKeepCollection.builder()
                        .value(DocumentToKeep.builder()
                            .documentId("456")
                            .caseDocument(CaseDocument.builder()
                                .documentLink(buildDocument("https://example2.com/456", "Form-D.pdf", "https://example2.com/binary", null))
                                .build())
                            .build())
                        .build()))
                .build();

            CaseData result = documentRemovalService.removeDocuments(caseData, 1L, "Auth");

            assertEquals(2, result.getClaimantResponseDocuments().size());
            assertNull(result.getDocumentToKeepCollection());
        }

        @Test
        void testRemoveDocuments_RemoveDocFromList() {
            List<Element<CaseDocument>> claimantResponseDocuments = new ArrayList<>();
            claimantResponseDocuments.add(element(buildCaseDocument(
                "https://example1.com/123", "Form-C.pdf", "https://example1.com/binary", null)));

            claimantResponseDocuments.add(element(buildCaseDocument(
                "https://example2.com/456", "Form-D.pdf", "https://example2.com/binary", null)));
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .claimantResponseDocuments(claimantResponseDocuments)
                .documentToKeepCollection(List.of(DocumentToKeepCollection.builder()
                        .value(DocumentToKeep.builder()
                            .documentId("123")
                            .caseDocument(CaseDocument.builder()
                                .documentLink(buildDocument("https://example1.com/123", "Form-C.pdf", "https://example1.com/binary", null))
                                .build())
                            .build())
                        .build()))
                .build();

            CaseData result = documentRemovalService.removeDocuments(caseData, 1L, "Auth");

            assertEquals(1, result.getClaimantResponseDocuments().size());
            assertEquals("Form-C.pdf", result.getClaimantResponseDocuments().get(0).getValue().getDocumentLink().getDocumentFileName());
            assertNull(result.getDocumentToKeepCollection());
        }

        @Test
        void testRemoveDocuments_NestedObjectInDocumentList() {
            Document document1 = buildDocument(
                "https://example1.com/123", "Approved Order1.pdf", "https://example1.com/binary", null);
            DocumentWithName documentWithName1 = DocumentWithName.builder().documentName("Approved Order1.pdf").document(document1).createdBy("bill bob").build();

            Document document2 = buildDocument(
                "https://example2.com/456", "Additional Hearing Doc.pdf", "https://example2.com/binary", null);
            DocumentWithName documentWithName2 = DocumentWithName.builder().documentName("Additional Hearing Doc.pdf").document(document2).createdBy("bill bob").build();

            List<Element<DocumentWithName>> documentsList = wrapElements(documentWithName1, documentWithName2);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .caseNoteType(CaseNoteType.DOCUMENT_ONLY)
                .documentAndNameToAdd(documentsList)
                .documentToKeepCollection(List.of(DocumentToKeepCollection.builder()
                    .value(DocumentToKeep.builder()
                        .documentId("456")
                        .caseDocument(CaseDocument.builder()
                            .documentLink(buildDocument("https://example2.com/456", "Additional Hearing Doc.pdf", "https://example2.com/binary", null))
                            .build())
                        .build())
                    .build()))
                .build();

            CaseData result = documentRemovalService.removeDocuments(caseData, 1L, "Auth");

            assertEquals(1, result.getDocumentAndNameToAdd().size());
            assertEquals("Additional Hearing Doc.pdf", result.getDocumentAndNameToAdd().get(0).getValue().getDocument().getDocumentFileName());
            assertNull(result.getDocumentToKeepCollection());
        }
    }
}