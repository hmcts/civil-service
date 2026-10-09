package uk.gov.hmcts.reform.civil.documentmanagement;

public class DocumentTooLargeException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Document %s is %d bytes, which exceeds the download limit of %d bytes.";

    public DocumentTooLargeException(String documentPath, long size, long maxSize) {
        super(String.format(MESSAGE_TEMPLATE, documentPath, size, maxSize));
    }
}
