package uk.gov.hmcts.reform.civil.service.documentmanagement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.service.UserService;
import java.time.LocalDateTime;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SETTLE_CLAIM_PAID_IN_FULL_LETTER;

@ExtendWith(SpringExtension.class)
public class DocumentDownloadServiceTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    @Mock
    private UserService userService;
    @Mock
    private DocumentManagementService documentManagementService;

    @InjectMocks
    private DocumentDownloadService documentDownloadService;

    final CaseDocument caseDocument =
        CaseDocument.builder()
            .createdBy("John")
            .documentName("Stitched document")
            .documentSize(0L)
            .documentType(SETTLE_CLAIM_PAID_IN_FULL_LETTER)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

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
        Arrays.equals(actualDoc, expectedDoc);
    }

    @Test
    void testDownloadDocumentByteArray_Error() {
        // given
        String documentId = "documentId";
        byte[] expectedDoc = "test".getBytes();
        ByteArrayResource expectedDocResources = new ByteArrayResource(expectedDoc);

        DownloadedDocumentResponse downloadedDoc = new DownloadedDocumentResponse(expectedDocResources,
                                           "test", "test");

        when(userService.getAccessToken(any(), any())).thenReturn("arbitrary access token");
        when(documentManagementService.downloadDocumentWithMetaData(anyString(), anyString())).thenThrow(new RuntimeException());

        assertThrows(DocumentDownloadException.class, () ->
            documentDownloadService.downloadDocument(caseDocument, BEARER_TOKEN, documentId, "error"));
    }
}
