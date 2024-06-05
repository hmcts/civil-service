package uk.gov.hmcts.reform.civil.exceptions;

public class StitchingFailedException extends RuntimeException {

    public StitchingFailedException(String message) {
        super(message);
    }
}
