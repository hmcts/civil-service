package uk.gov.hmcts.reform.civil.exceptions;

public class InvalidPaymentStatusException extends RuntimeException {

    public InvalidPaymentStatusException(String message) {
        super(message);
    }
}
