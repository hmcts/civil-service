package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;

@Component
public class UploadExpertEvidenceDocumentTypeBuilder implements DocumentTypeBuilder<UploadEvidenceExpert> {

    @Override
    public UploadEvidenceExpert buildElementTypeWithDocumentCopy(UploadEvidenceExpert fromValue, String categoryId) {
        Document newDoc = new Document();
        newDoc.setCategoryID(categoryId)
            .setDocumentBinaryUrl(fromValue.getExpertDocument().getDocumentBinaryUrl())
            .setDocumentFileName(fromValue.getExpertDocument().getDocumentFileName())
            .setDocumentHash(fromValue.getExpertDocument().getDocumentHash())
            .setDocumentUrl(fromValue.getExpertDocument().getDocumentUrl());
        UploadEvidenceExpert uploadEvidenceExpert = new UploadEvidenceExpert();
        uploadEvidenceExpert.setExpertOptionName(fromValue.getExpertOptionName());
        uploadEvidenceExpert.setExpertOptionExpertise(fromValue.getExpertOptionExpertise());
        uploadEvidenceExpert.setExpertOptionExpertises(fromValue.getExpertOptionExpertises());
        uploadEvidenceExpert.setExpertOptionOtherParty(fromValue.getExpertOptionOtherParty());
        uploadEvidenceExpert.setExpertDocumentQuestion(fromValue.getExpertDocumentQuestion());
        uploadEvidenceExpert.setExpertDocumentAnswer(fromValue.getExpertDocumentAnswer());
        uploadEvidenceExpert.setExpertOptionUploadDate(fromValue.getExpertOptionUploadDate());
        uploadEvidenceExpert.setExpertDocument(newDoc);
        return uploadEvidenceExpert;
    }
}
