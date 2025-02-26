package uk.gov.hmcts.reform.civil.notification;

public class EmailNotificationFailedException extends RuntimeException {

    public EmailNotificationFailedException(String message) {
        super(message);
    }
}
