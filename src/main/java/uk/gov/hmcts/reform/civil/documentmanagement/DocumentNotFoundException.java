package uk.gov.hmcts.reform.civil.documentmanagement;

public class DocumentNotFoundException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Document %s could not be found in document management.";

    public DocumentNotFoundException(String fileName, Throwable t) {
        super(String.format(MESSAGE_TEMPLATE, fileName), t);
    }
}
