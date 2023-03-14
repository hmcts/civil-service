package uk.gov.hmcts.reform.civil.notify;

public class NotificationException extends RuntimeException {

    public NotificationException(Exception cause) {
        super(cause);
    }

}
