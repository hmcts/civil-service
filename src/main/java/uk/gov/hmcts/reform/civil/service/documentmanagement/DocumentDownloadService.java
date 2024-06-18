package uk.gov.hmcts.reform.civil.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.model.documents.DownloadedDocumentResponse;

@Service
@RequiredArgsConstructor
public class DocumentDownloadService {

    private final DocumentManagementService documentManagementService;

    public DownloadedDocumentResponse downloadDocument(String authorisation, String documentId) {
        String documentPath = String.format("documents/%s", documentId);
        return documentManagementService.downloadDocumentWithMetaData(authorisation, documentPath);
    }
}
