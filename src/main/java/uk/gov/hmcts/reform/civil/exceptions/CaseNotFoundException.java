package uk.gov.hmcts.reform.civil.exceptions;

public class CaseNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Case was not found";

    public CaseNotFoundException() {
        super(ERROR_MESSAGE);
    }
}
