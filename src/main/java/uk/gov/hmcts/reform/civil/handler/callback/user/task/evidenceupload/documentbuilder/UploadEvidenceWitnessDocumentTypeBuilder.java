package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;

@Component
public class UploadEvidenceWitnessDocumentTypeBuilder implements DocumentTypeBuilder<UploadEvidenceWitness> {

    @Override
    public UploadEvidenceWitness buildElementTypeWithDocumentCopy(UploadEvidenceWitness fromValue, String categoryID) {
        Document newDoc = new Document();
        newDoc.setCategoryID(categoryID)
            .setDocumentBinaryUrl(fromValue.getWitnessOptionDocument().getDocumentBinaryUrl())
            .setDocumentFileName(fromValue.getWitnessOptionDocument().getDocumentFileName())
            .setDocumentHash(fromValue.getWitnessOptionDocument().getDocumentHash())
            .setDocumentUrl(fromValue.getWitnessOptionDocument().getDocumentUrl());
        UploadEvidenceWitness uploadEvidenceWitness = new UploadEvidenceWitness();
        uploadEvidenceWitness.setWitnessOptionUploadDate(fromValue.getWitnessOptionUploadDate());
        uploadEvidenceWitness.setWitnessOptionName(fromValue.getWitnessOptionName());
        uploadEvidenceWitness.setCreatedDatetime(fromValue.getCreatedDatetime());
        uploadEvidenceWitness.setWitnessOptionDocument(newDoc);
        return uploadEvidenceWitness;
    }
}
