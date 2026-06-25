package uk.gov.hmcts.reform.civil.documentmanagement;

public class InvalidDocumentReferenceException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Invalid document reference: %s";

    public InvalidDocumentReferenceException(String documentPath) {
        super(String.format(MESSAGE_TEMPLATE, documentPath));
    }

    public InvalidDocumentReferenceException(String documentPath, Throwable t) {
        super(String.format(MESSAGE_TEMPLATE, documentPath), t);
    }
}
