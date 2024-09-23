package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;

public abstract class RespondentSolicitorOneDocumentHandler<T> extends LegalRepresentativeOneDocumentHandler<T> {
    public RespondentSolicitorOneDocumentHandler(DocumentCategory documentCategory, DocumentCategory legalRepresentativeTwoDocumentCategory,
                                                 String documentNotificationText,
                                                 DocumentTypeBuilder documentTypeBuilder) {
        super(documentCategory, legalRepresentativeTwoDocumentCategory, documentNotificationText, documentTypeBuilder);
    }
}
