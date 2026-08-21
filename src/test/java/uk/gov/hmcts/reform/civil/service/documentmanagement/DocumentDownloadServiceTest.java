package uk.gov.hmcts.reform.civil.service.documentmanagement;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SETTLE_CLAIM_PAID_IN_FULL_LETTER;

@ExtendWith(SpringExtension.class)
class DocumentDownloadServiceTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    @Mock
    private UserService userService;
    @Mock
    private DocumentManagementService documentManagementService;

    @InjectMocks
    private DocumentDownloadService documentDownloadService;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setup() {
        logger = (Logger) LoggerFactory.getLogger(DocumentDownloadService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    final CaseDocument caseDocument =
        new CaseDocument()
            .setCreatedBy("John")
            .setDocumentName("Stitched document")
            .setDocumentSize(0L)
            .setDocumentType(SETTLE_CLAIM_PAID_IN_FULL_LETTER)
            .setCreatedDatetime(LocalDateTime.now())
            .setDocumentLink(new Document()
                              .setDocumentUrl("fake-url")
                              .setDocumentFileName("file-name")
                              .setDocumentBinaryUrl("binary-url"));

    @Test
    void testDownloadDocumentById() {
        // given
        String documentId = "documentId";
        DownloadedDocumentResponse expectedDoc = new DownloadedDocumentResponse(new ByteArrayResource("test".getBytes()),
                                                                                "test",
                                                                                "test"
        );
        when(userService.getAccessToken(any(), any())).thenReturn("arbitrary access token");
        when(documentManagementService.downloadDocumentWithMetaData(anyString(), anyString())).thenReturn(expectedDoc);
        // when
        DownloadedDocumentResponse downloadedDoc = documentDownloadService.downloadDocument(BEARER_TOKEN, documentId);
        //Then
        assertEquals(downloadedDoc, expectedDoc);
    }

    @Test
    void testDownloadDocumentByteArray() {
        // given
        String documentId = "documentId";
        byte[] expectedDoc = "test".getBytes();
        ByteArrayResource expectedDocResources = new ByteArrayResource(expectedDoc);

        DownloadedDocumentResponse downloadedDoc = new DownloadedDocumentResponse(expectedDocResources,
                                           "test", "test");

        when(userService.getAccessToken(any(), any())).thenReturn("arbitrary access token");
        when(documentManagementService.downloadDocumentWithMetaData(anyString(), anyString())).thenReturn(downloadedDoc);

        // when
        byte[] actualDoc = documentDownloadService.downloadDocument(caseDocument, BEARER_TOKEN, documentId, "error");
        //Then
        assertArrayEquals(actualDoc, expectedDoc);
    }

    @Test
    void testDownloadDocumentByteArray_Error() {
        // given
        String documentId = "documentId";
        when(userService.getAccessToken(any(), any())).thenReturn("arbitrary access token");
        when(documentManagementService.downloadDocumentWithMetaData(anyString(), anyString())).thenThrow(new RuntimeException());

        assertThrows(DocumentDownloadException.class, () ->
            documentDownloadService.downloadDocument(caseDocument, BEARER_TOKEN, documentId, "error"));
    }

    @Test
    void shouldLogCaseIdAndDocumentReferenceWhenDownloadFails() {
        String caseId = "1234567890123456";
        CaseDocument document = new CaseDocument()
            .setDocumentLink(new Document()
                                 .setDocumentUrl("http://dm-store/documents/document-uuid")
                                 .setDocumentFileName("file-name"));
        when(documentManagementService.downloadDocumentWithMetaData(anyString(), anyString()))
            .thenThrow(new RuntimeException("CDAM metadata 404"));

        assertThrows(DocumentDownloadException.class, () ->
            documentDownloadService.downloadDocument(document, BEARER_TOKEN, caseId, "error for case {}"));

        assertTrue(listAppender.list.stream()
                       .map(ILoggingEvent::getFormattedMessage)
                       .anyMatch(message -> message.contains(
                           "Failed downloading document document-uuid from http://dm-store/documents/document-uuid for case " + caseId
                       )));
    }
}
