package uk.gov.hmcts.reform.civil.stateflow.exception;

import uk.gov.hmcts.reform.civil.exceptions.NotRetryableException;

public class StateFlowException extends NotRetryableException {

    public StateFlowException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateFlowException(String message) {
        super(message);
    }
}
