package uk.gov.hmcts.reform.civil.exceptions;

public class MissingFieldsUpdatedException extends Exception {

    public MissingFieldsUpdatedException() {
        super("Missing fields updated");
    }
}
