package uk.gov.hmcts.reform.civil.exceptions;

public class CaseDataInvalidException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Case data invalid";

    public CaseDataInvalidException() {
        super(ERROR_MESSAGE);
    }
}
