package uk.gov.hmcts.reform.civil.documentmanagement;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;

public interface DocumentManagementService {

    CaseDocument uploadDocument(String authorisation, PDF pdf);

    byte[] downloadDocument(String authorisation, String documentPath);

    ResponseEntity<Resource> downloadDocumentByDocumentPath(String authorisation, String documentPath);

}
