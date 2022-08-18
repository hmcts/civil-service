package uk.gov.hmcts.reform.civil.service.search.exceptions;

public class CaseNotFoundException extends RuntimeException{

    private static final String ERROR_MESSAGE = "Claim not found";

    public CaseNotFoundException () {
        super(ERROR_MESSAGE);
    }
}
