package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;

public abstract class ApplicantSolicitorOneDocumentHandler<T> extends LegalRepresentativeOneDocumentHandler<T> {
    public ApplicantSolicitorOneDocumentHandler(DocumentCategory documentCategory, DocumentCategory legalRepresentativeTwoDocumentCategory,
                                                String documentNotificationText,
                                                DocumentTypeBuilder documentTypeBuilder) {
        super(documentCategory, legalRepresentativeTwoDocumentCategory, documentNotificationText, documentTypeBuilder);
    }
}
