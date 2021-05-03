package uk.gov.hmcts.reform.civil.service.documentmanagement;

public class DocumentUploadException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Unable to upload document %s to document management.";

    public DocumentUploadException(String fileName) {
        super(String.format(MESSAGE_TEMPLATE, fileName));
    }

    public DocumentUploadException(String fileName, Throwable t) {
        super(String.format(MESSAGE_TEMPLATE, fileName), t);
    }
}
