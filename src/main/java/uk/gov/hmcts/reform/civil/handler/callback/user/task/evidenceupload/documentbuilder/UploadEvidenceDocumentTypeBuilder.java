package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;

@Component
public class UploadEvidenceDocumentTypeBuilder implements DocumentTypeBuilder<UploadEvidenceDocumentType> {

    @Override
    public UploadEvidenceDocumentType buildElementTypeWithDocumentCopy(UploadEvidenceDocumentType fromValue, String categoryId) {
        Document newDoc = Document.builder()
            .categoryID(categoryId)
            .documentBinaryUrl(fromValue.getDocumentUpload().getDocumentBinaryUrl())
            .documentFileName(fromValue.getDocumentUpload().getDocumentFileName())
            .documentHash(fromValue.getDocumentUpload().getDocumentHash())
            .documentUrl(fromValue.getDocumentUpload().getDocumentUrl())
            .build();
        return UploadEvidenceDocumentType.builder()
            .witnessOptionName(fromValue.getWitnessOptionName())
            .documentIssuedDate(fromValue.getDocumentIssuedDate())
            .typeOfDocument(fromValue.getTypeOfDocument())
            .createdDatetime(fromValue.getCreatedDatetime())
            .documentUpload(newDoc)
            .build();
    }
}
