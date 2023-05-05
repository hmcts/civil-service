package uk.gov.hmcts.reform.hearings.jms.exception;

public class HmcEventProcessingException extends Exception {

    public HmcEventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
