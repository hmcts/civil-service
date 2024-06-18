package uk.gov.hmcts.reform.civil.model.citizenui;

import uk.gov.hmcts.reform.civil.model.documents.DocumentType;

public final class DocumentTypeMapper {

    private DocumentTypeMapper() {
    }

    public static ManageDocumentType mapDocumentTypeToManageDocumentType(DocumentType documentType) {
        if (documentType == DocumentType.MEDIATION_AGREEMENT) {
            return ManageDocumentType.MEDIATION_AGREEMENT;
        }
        return null;
    }
}
