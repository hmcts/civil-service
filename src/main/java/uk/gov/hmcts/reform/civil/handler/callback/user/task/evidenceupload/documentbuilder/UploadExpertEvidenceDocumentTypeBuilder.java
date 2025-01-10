package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;

@Component
public class UploadExpertEvidenceDocumentTypeBuilder implements DocumentTypeBuilder<UploadEvidenceExpert> {

    @Override
    public UploadEvidenceExpert buildElementTypeWithDocumentCopy(UploadEvidenceExpert fromValue, String categoryId) {
        Document newDoc = Document.builder()
            .categoryID(categoryId)
            .documentBinaryUrl(fromValue.getExpertDocument().getDocumentBinaryUrl())
            .documentFileName(fromValue.getExpertDocument().getDocumentFileName())
            .documentHash(fromValue.getExpertDocument().getDocumentHash())
            .documentUrl(fromValue.getExpertDocument().getDocumentUrl())
            .build();
        return UploadEvidenceExpert.builder()
            .expertOptionName(fromValue.getExpertOptionName())
            .expertOptionExpertise(fromValue.getExpertOptionExpertise())
            .expertOptionExpertises(fromValue.getExpertOptionExpertises())
            .expertOptionOtherParty(fromValue.getExpertOptionOtherParty())
            .expertDocumentQuestion(fromValue.getExpertDocumentQuestion())
            .expertDocumentAnswer(fromValue.getExpertDocumentAnswer())
            .expertOptionUploadDate(fromValue.getExpertOptionUploadDate())
            .expertDocument(newDoc)
            .build();
    }
}
