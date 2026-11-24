package uk.gov.hmcts.reform.civil.exceptions;

public class GaNotificationHistoryException extends RuntimeException {

    public GaNotificationHistoryException(String caseId, Throwable cause) {
        super("Failed to record GA notification history for case " + caseId, cause);
    }
}
