package uk.gov.hmcts.reform.civil.exceptions;

public class DocumentConversionException extends RuntimeException {

    public DocumentConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
