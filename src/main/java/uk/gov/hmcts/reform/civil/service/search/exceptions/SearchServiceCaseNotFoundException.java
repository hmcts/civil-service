package uk.gov.hmcts.reform.civil.service.search.exceptions;

public class SearchServiceCaseNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Claim not found";

    public SearchServiceCaseNotFoundException() {
        super(ERROR_MESSAGE);
    }
}
