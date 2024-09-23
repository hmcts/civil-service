package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

public abstract class ApplicantSolicitorTwoDocumentHandler<T> extends DocumentHandler<T>{
    public ApplicantSolicitorTwoDocumentHandler(DocumentCategory documentCategory, String documentNotificationText) {
        super(documentCategory, documentNotificationText);
    }
}
