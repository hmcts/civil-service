package uk.gov.hmcts.reform.civil.exceptions;

public class RetryableClaimStoreException extends PaymentsApiException {

    public RetryableClaimStoreException(String message) {
        super(message);
    }

    public RetryableClaimStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
