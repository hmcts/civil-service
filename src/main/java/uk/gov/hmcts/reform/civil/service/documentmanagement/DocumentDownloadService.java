package uk.gov.hmcts.reform.civil.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDownloadService {

    private final DocumentManagementService documentManagementService;

    public DownloadedDocumentResponse downloadDocument(String authorisation, String documentId) {
        String documentPath = String.format("documents/%s", documentId);
        return documentManagementService.downloadDocumentWithMetaData(authorisation, documentPath);
    }

    public byte[] downloadDocument(CaseDocument mailableSdoDocument, String authorisation, String caseId, String errorMessage) {
        byte[] letterContent;
        String documentUrl = mailableSdoDocument.getDocumentLink().getDocumentUrl();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

        try {
            letterContent = downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (Exception e) {
            log.error(errorMessage, caseId, e);
            throw new DocumentDownloadException(mailableSdoDocument.getDocumentLink().getDocumentFileName(), e);
        }
        return letterContent;
    }
}
