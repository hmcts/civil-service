package uk.gov.hmcts.reform.civil.documentmanagement;

import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.documentmanagement.model.UploadedDocument;

import java.util.List;

public interface DocumentManagementService {

    CaseDocument uploadDocument(String authorisation, PDF pdf);

    CaseDocument uploadDocument(String authorisation, UploadedDocument uploadedDocument);

    byte[] downloadDocument(String authorisation, String documentPath);

}
