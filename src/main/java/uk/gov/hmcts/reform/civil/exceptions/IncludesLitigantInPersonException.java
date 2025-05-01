package uk.gov.hmcts.reform.civil.exceptions;

public class IncludesLitigantInPersonException extends RuntimeException {

    private static final String ERROR_MESSAGE = "This action cannot be completed on cases with Litigants in Person.";

    public IncludesLitigantInPersonException() {
        super(ERROR_MESSAGE);
    }

}
