package uk.gov.hmcts.reform.civil.documentmanagement;

import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;

public interface DocumentManagementService {

    CaseDocument uploadDocument(String authorisation, PDF pdf);

    byte[] downloadDocument(String authorisation, String documentPath);

    DocumentResponse downloadDocumentByDocumentPath(String authorisation, String documentPath);

}
