package uk.gov.hmcts.reform.civil.exceptions;

public class UserNotFoundOnCaseException extends RuntimeException {

    private static final String ERROR_MESSAGE = "%s was found on case";

    public UserNotFoundOnCaseException(String userName) {
        super(String.format(ERROR_MESSAGE, userName));
    }
}
