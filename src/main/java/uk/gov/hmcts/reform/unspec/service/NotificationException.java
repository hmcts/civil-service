package uk.gov.hmcts.reform.unspec.service;

public class NotificationException extends RuntimeException {

    public NotificationException(Exception cause) {
        super(cause);
    }

}
