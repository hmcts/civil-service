package uk.gov.hmcts.reform.civil.documentmanagement;

public class DocumentTtlExpiredException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Document %s can not be accessed because its TTL has expired.";

    public DocumentTtlExpiredException(String documentPath, Throwable cause) {
        super(String.format(MESSAGE_TEMPLATE, documentPath), cause);
    }
}
