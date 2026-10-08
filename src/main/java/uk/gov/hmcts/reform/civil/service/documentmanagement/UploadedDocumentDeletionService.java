package uk.gov.hmcts.reform.civil.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentAccessException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.InvalidDocumentLinkException;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadedDocumentDeletionService {

    private final DocumentManagementService documentManagementService;
    private final UserService userService;

    public void deleteOwnDocument(String authorisation, String documentId) {
        String documentPath = String.format("documents/%s", documentId);
        try {
            UUID.fromString(documentId);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidDocumentLinkException(documentPath, ex);
        }

        Document metadata = documentManagementService.getDocumentMetaData(authorisation, documentPath);
        String userId = userService.getUserInfo(authorisation).getUid();
        if (userId == null || !userId.equals(metadata.createdBy)) {
            log.warn("User {} is not the creator of document {}, refusing delete", userId, documentId);
            throw new DocumentAccessException(documentPath, null);
        }
        if (metadata.ttl == null) {
            log.warn("Document {} is attached to a case, refusing delete by user {}", documentId, userId);
            throw new DocumentAccessException(documentPath, null);
        }

        documentManagementService.deleteDocument(authorisation, documentPath);
    }
}
