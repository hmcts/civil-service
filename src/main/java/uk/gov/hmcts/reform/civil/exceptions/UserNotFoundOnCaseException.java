package uk.gov.hmcts.reform.civil.exceptions;

public class UserNotFoundOnCaseException extends RuntimeException {

    private static final String ERROR_MESSAGE = "User with Id: %s was not found on case";

    public UserNotFoundOnCaseException(String userId) {
        super(String.format(ERROR_MESSAGE, userId));
    }
}
