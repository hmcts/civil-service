package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;

@Component
public class UploadEvidenceDocumentTypeBuilder implements DocumentTypeBuilder<UploadEvidenceDocumentType> {

    @Override
    public UploadEvidenceDocumentType buildElementTypeWithDocumentCopy(UploadEvidenceDocumentType fromValue, String categoryId) {
        Document newDoc = new Document();
        newDoc.setCategoryID(categoryId);
        newDoc.setDocumentBinaryUrl(fromValue.getDocumentUpload().getDocumentBinaryUrl());
        newDoc.setDocumentFileName(fromValue.getDocumentUpload().getDocumentFileName());
        newDoc.setDocumentHash(fromValue.getDocumentUpload().getDocumentHash());
        newDoc.setDocumentUrl(fromValue.getDocumentUpload().getDocumentUrl());
        return new UploadEvidenceDocumentType()
            .setWitnessOptionName(fromValue.getWitnessOptionName())
            .setDocumentIssuedDate(fromValue.getDocumentIssuedDate())
            .setTypeOfDocument(fromValue.getTypeOfDocument())
            .setCreatedDatetime(fromValue.getCreatedDatetime())
            .setDocumentUpload(newDoc);
    }
}
