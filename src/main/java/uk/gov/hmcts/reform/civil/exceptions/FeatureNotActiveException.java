package uk.gov.hmcts.reform.civil.exceptions;

public class FeatureNotActiveException extends RuntimeException {

    private static final String ERROR_MESSAGE = "%s feature is not active.";

    public FeatureNotActiveException(String featureName) {
        super(String.format(ERROR_MESSAGE, featureName));
    }
}
