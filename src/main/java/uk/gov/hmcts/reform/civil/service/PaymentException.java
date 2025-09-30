package uk.gov.hmcts.reform.civil.service;

public class PaymentException extends Exception {

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
