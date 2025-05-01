package uk.gov.hmcts.reform.civil.exceptions;

public class RetryableStitchingException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Stitching failed, retrying...";

    public RetryableStitchingException() {
        super(ERROR_MESSAGE);
    }
}
