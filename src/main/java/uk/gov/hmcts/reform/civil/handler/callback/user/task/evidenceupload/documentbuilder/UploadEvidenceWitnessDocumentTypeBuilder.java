package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;

@Component
public class UploadEvidenceWitnessDocumentTypeBuilder implements DocumentTypeBuilder<UploadEvidenceWitness> {

    @Override
    public UploadEvidenceWitness buildElementTypeWithDocumentCopy(UploadEvidenceWitness fromValue, String categoryID) {
        Document newDoc = Document.builder()
            .categoryID(categoryID)
            .documentBinaryUrl(fromValue.getWitnessOptionDocument().getDocumentBinaryUrl())
            .documentFileName(fromValue.getWitnessOptionDocument().getDocumentFileName())
            .documentHash(fromValue.getWitnessOptionDocument().getDocumentHash())
            .documentUrl(fromValue.getWitnessOptionDocument().getDocumentUrl())
            .build();
        return UploadEvidenceWitness.builder()
            .witnessOptionUploadDate(fromValue.getWitnessOptionUploadDate())
            .witnessOptionName(fromValue.getWitnessOptionName())
            .createdDatetime(fromValue.getCreatedDatetime())
            .witnessOptionDocument(newDoc)
            .build();
    }
}
