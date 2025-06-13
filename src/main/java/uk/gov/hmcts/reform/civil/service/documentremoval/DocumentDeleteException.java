package uk.gov.hmcts.reform.civil.service.documentremoval;

public class DocumentDeleteException extends RuntimeException {

    public DocumentDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
