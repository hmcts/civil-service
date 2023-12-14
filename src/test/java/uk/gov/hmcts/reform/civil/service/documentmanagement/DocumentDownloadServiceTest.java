package uk.gov.hmcts.reform.civil.service.documentmanagement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class DocumentDownloadServiceTest {

    private static final String BEARER_TOKEN = "Bearer Token";
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
}
