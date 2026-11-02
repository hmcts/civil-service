package uk.gov.hmcts.reform.civil.service.documentmanagement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentAccessException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.InvalidDocumentLinkException;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadedDocumentDeletionServiceTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String DOCUMENT_ID = "85d97996-22a5-40d7-882e-3a382c8ae1b7";
    private static final String DOCUMENT_PATH = "documents/" + DOCUMENT_ID;
    private static final String USER_ID = "user-id";

    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private UserService userService;

    @InjectMocks
    private UploadedDocumentDeletionService uploadedDocumentDeletionService;

    @Test
    void shouldDeleteDocument_whenCallerCreatedIt() {
        when(documentManagementService.getDocumentMetaData(BEARER_TOKEN, DOCUMENT_PATH))
            .thenReturn(Document.builder().createdBy(USER_ID).ttl(new Date()).build());
        when(userService.getUserInfo(BEARER_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());

        uploadedDocumentDeletionService.deleteOwnDocument(BEARER_TOKEN, DOCUMENT_ID);

        verify(documentManagementService).deleteDocument(BEARER_TOKEN, DOCUMENT_PATH);
    }

    @Test
    void shouldRefuseDelete_whenDocumentAlreadyAttachedToCase() {
        when(documentManagementService.getDocumentMetaData(BEARER_TOKEN, DOCUMENT_PATH))
            .thenReturn(Document.builder().createdBy(USER_ID).build());
        when(userService.getUserInfo(BEARER_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());

        assertThrows(
            DocumentAccessException.class,
            () -> uploadedDocumentDeletionService.deleteOwnDocument(BEARER_TOKEN, DOCUMENT_ID)
        );
        verify(documentManagementService, never()).deleteDocument(anyString(), anyString());
    }

    @Test
    void shouldRefuseDelete_whenDocumentCreatedBySomeoneElse() {
        when(documentManagementService.getDocumentMetaData(BEARER_TOKEN, DOCUMENT_PATH))
            .thenReturn(Document.builder().createdBy("another-user").build());
        when(userService.getUserInfo(BEARER_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());

        assertThrows(
            DocumentAccessException.class,
            () -> uploadedDocumentDeletionService.deleteOwnDocument(BEARER_TOKEN, DOCUMENT_ID)
        );
        verify(documentManagementService, never()).deleteDocument(anyString(), anyString());
    }

    @Test
    void shouldRefuseDelete_whenCallerHasNoUserId() {
        when(documentManagementService.getDocumentMetaData(BEARER_TOKEN, DOCUMENT_PATH))
            .thenReturn(Document.builder().build());
        when(userService.getUserInfo(BEARER_TOKEN)).thenReturn(UserInfo.builder().build());

        assertThrows(
            DocumentAccessException.class,
            () -> uploadedDocumentDeletionService.deleteOwnDocument(BEARER_TOKEN, DOCUMENT_ID)
        );
        verify(documentManagementService, never()).deleteDocument(anyString(), anyString());
    }

    @Test
    void shouldRejectDocumentIdThatIsNotAUuid() {
        assertThrows(
            InvalidDocumentLinkException.class,
            () -> uploadedDocumentDeletionService.deleteOwnDocument(BEARER_TOKEN, "../../something")
        );
        verifyNoInteractions(documentManagementService, userService);
    }

    @Test
    void shouldRejectNullDocumentId() {
        assertThrows(
            InvalidDocumentLinkException.class,
            () -> uploadedDocumentDeletionService.deleteOwnDocument(BEARER_TOKEN, null)
        );
        verifyNoInteractions(documentManagementService, userService);
    }
}
