package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.experimental.UtilityClass;

import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

@UtilityClass
public final class DocumentTypeMapper {

    public static ManageDocumentType mapDocumentTypeToManageDocumentType(DocumentType documentType) {
        switch (documentType) {
            case MEDIATION_AGREEMENT:
                return ManageDocumentType.MEDIATION_AGREEMENT;
            default:
                return null;
        }
    }
}
