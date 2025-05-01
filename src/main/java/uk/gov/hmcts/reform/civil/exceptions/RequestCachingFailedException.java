package uk.gov.hmcts.reform.civil.exceptions;

public class RequestCachingFailedException extends RuntimeException {

    public RequestCachingFailedException(String message) {
        super(message);
    }
}
