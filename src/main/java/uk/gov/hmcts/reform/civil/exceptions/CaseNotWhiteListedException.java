package uk.gov.hmcts.reform.civil.exceptions;

public class CaseNotWhiteListedException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Case location has not been whitelisted for case progression";

    public CaseNotWhiteListedException() {
        super(ERROR_MESSAGE);
    }
}
