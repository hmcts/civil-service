package uk.gov.hmcts.reform.migration.domain.exception;

public class MigrationLimitReachedException extends RuntimeException {
    public MigrationLimitReachedException(String message) {
        super(message);
    }
}
