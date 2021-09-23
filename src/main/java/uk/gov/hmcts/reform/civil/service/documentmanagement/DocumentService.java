package uk.gov.hmcts.reform.civil.service.documentmanagement;

import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.PDF;

public interface DocumentService {

    CaseDocument uploadDocument(String authorisation, PDF pdf);

    byte[] downloadDocument(String authorisation, String documentPath);

}
