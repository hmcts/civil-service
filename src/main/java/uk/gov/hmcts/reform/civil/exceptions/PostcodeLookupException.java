package uk.gov.hmcts.reform.civil.exceptions;

public class PostcodeLookupException extends RuntimeException {

    public PostcodeLookupException(String message, Throwable cause) {
        super(message, cause);
    }

}
