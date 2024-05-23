package uk.gov.hmcts.reform.civil.service.documentmanagement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.DocumentLoaderUtil.loadResource;

@ExtendWith(SpringExtension.class)
class DocumentDownloadServiceTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private byte[] someBytes = "civil-service".getBytes();
    @Mock
    private UserService userService;
    @Mock
    private DocumentManagementService documentManagementService;

    @InjectMocks
    private DocumentDownloadService documentDownloadService;

    @Test
    void testDownloadDocumentById() {
        // given
        String documentId = "documentId";
        DownloadedDocumentResponse expectedDoc =
            new DownloadedDocumentResponse(new ByteArrayResource("test".getBytes()),
                                           "test", "test");
        when(userService.getAccessToken(any(), any())).thenReturn("arbitrary access token");
        when(documentManagementService.downloadDocumentWithMetaData(anyString(), anyString())).thenReturn(expectedDoc);

        // when
        DownloadedDocumentResponse downloadedDoc = documentDownloadService.downloadDocument(BEARER_TOKEN, documentId);
        //Then
        assertEquals(downloadedDoc, expectedDoc);
    }

    @Test
    void validatePasswordProtectedDocumentUploadedThenThrowPasswordProtectedMessage() throws IOException {
        String fixture = "/fixture/go1protected.pdf";
        byte[] bytes = loadResource(fixture);

        DownloadedDocumentResponse expectedDoc =
            new DownloadedDocumentResponse(new ByteArrayResource(bytes),
                                           "go1protected.pdf", "test");
        when(userService.getAccessToken(any(), any())).thenReturn("arbitrary access token");
        when(documentManagementService.downloadDocumentWithMetaData(anyString(), anyString())).thenReturn(expectedDoc);
        Document document = Document.builder().documentUrl("http://test/9939").documentFileName("go1protected.pdf").build();
        List<String> errors = new ArrayList<>();
        documentDownloadService.validateEncryptionOnUploadedDocument(document, BEARER_TOKEN, 1234L, errors);
        assertEquals("Uploaded document 'go1protected.pdf' is password protected. "
                         + "Please remove password and try uploading again.", errors.get(0));
    }

    @Test
    void validateInvalidDocumentUploadedThenThrowParseErrorMessage() {
        DownloadedDocumentResponse expectedDoc =
            new DownloadedDocumentResponse(new ByteArrayResource(someBytes),
                                           "go1protected.pdf", "test");
        when(userService.getAccessToken(any(), any())).thenReturn("arbitrary access token");
        when(documentManagementService.downloadDocumentWithMetaData(anyString(), anyString())).thenReturn(expectedDoc);
        Document document = Document.builder().documentUrl("http://test/9939").documentFileName("go1protected.pdf").build();
        List<String> errors = new ArrayList<>();
        documentDownloadService.validateEncryptionOnUploadedDocument(document, BEARER_TOKEN, 1234L, errors);
        assertEquals("Failed to parse the documents go1protected.pdf for caseId1234;" +
                         " Error: End-of-File, expected line at offset 13", errors.get(0));
    }

    @Test
    void validateNoByteDocumentUploadedThenThrowErrorMessage() {
        when(userService.getAccessToken(any(), any())).thenReturn("arbitrary access token");
        when(documentManagementService.downloadDocumentWithMetaData(anyString(), anyString())).thenThrow(
            DocumentDownloadException.class);
        Document document = Document.builder().documentUrl("http://test/9939").documentFileName("go1protected.pdf").build();
        List<String> errors = new ArrayList<>();

        assertThatThrownBy(() ->
                               documentDownloadService.validateEncryptionOnUploadedDocument(document, BEARER_TOKEN, 1234L, errors)
        ).isInstanceOf(DocumentDownloadException.class)
            .hasMessage("Unable to download document go1protected.pdf from document management.");
    }
}
