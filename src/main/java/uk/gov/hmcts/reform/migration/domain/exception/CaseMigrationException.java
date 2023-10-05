package uk.gov.hmcts.reform.migration.domain.exception;

public class CaseMigrationException extends RuntimeException {

    public CaseMigrationException(String message) {
        super(message);
    }
}
