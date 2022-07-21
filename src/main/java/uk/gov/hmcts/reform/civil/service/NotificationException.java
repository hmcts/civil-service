package uk.gov.hmcts.reform.civil.service;

public class NotificationException extends RuntimeException {

    public NotificationException(Exception cause) {
        super(cause);
    }

    public NotificationException(String message) {
        super(new RuntimeException(message));
    }

}
