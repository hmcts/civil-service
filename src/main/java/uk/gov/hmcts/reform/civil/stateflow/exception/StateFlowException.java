package uk.gov.hmcts.reform.civil.stateflow.exception;

public class StateFlowException extends RuntimeException {

    public StateFlowException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateFlowException(String message) {
        super(message);
    }
}
