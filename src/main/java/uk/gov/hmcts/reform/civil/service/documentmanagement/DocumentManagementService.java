package uk.gov.hmcts.reform.civil.service.documentmanagement;

import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.model.documents.UploadedDocument;

public interface DocumentManagementService {

    CaseDocument uploadDocument(String authorisation, PDF pdf);

    CaseDocument uploadDocument(String authorisation, UploadedDocument uploadedDocument);

    byte[] downloadDocument(String authorisation, String documentPath);

    DownloadedDocumentResponse downloadDocumentWithMetaData(String authorisation, String documentPath);

}
