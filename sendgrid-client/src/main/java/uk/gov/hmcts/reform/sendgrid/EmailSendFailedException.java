package uk.gov.hmcts.reform.sendgrid;

public class EmailSendFailedException extends RuntimeException {

    public EmailSendFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailSendFailedException(Throwable cause) {
        super(cause);
    }
}
