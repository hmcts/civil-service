package uk.gov.hmcts.reform.civil.documentmanagement;

import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.documentmanagement.model.UploadedDocument;

import java.time.LocalDateTime;

public interface DocumentManagementService {

    CaseDocument uploadDocument(String authorisation, PDF pdf);

    CaseDocument uploadDocument(String authorisation, UploadedDocument uploadedDocument);

    byte[] downloadDocument(String authorisation, String documentPath);

    DownloadedDocumentResponse downloadDocumentWithMetaData(String authorisation, String documentPath);

    void updateDocumentTimeToLive(String documentPath, String authorisation, LocalDateTime datetime);

}
