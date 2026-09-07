package uk.gov.hmcts.reform.civil.documentmanagement;

public class DocumentAccessException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Access to document %s was refused by document management.";

    public DocumentAccessException(String fileName, Throwable t) {
        super(String.format(MESSAGE_TEMPLATE, fileName), t);
    }
}
