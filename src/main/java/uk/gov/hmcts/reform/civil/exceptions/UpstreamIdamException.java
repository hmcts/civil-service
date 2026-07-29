package uk.gov.hmcts.reform.civil.exceptions;

public class UpstreamIdamException extends RuntimeException {

    public UpstreamIdamException(String message) {
        super(message);
    }

    public UpstreamIdamException(String message, Throwable cause) {
        super(message, cause);
    }
}
