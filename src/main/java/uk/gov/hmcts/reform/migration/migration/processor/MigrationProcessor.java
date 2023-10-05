package uk.gov.hmcts.reform.migration.migration.processor;

import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.migration.domain.exception.CaseMigrationException;
import uk.gov.hmcts.reform.migration.domain.exception.MigrationLimitReachedException;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public interface MigrationProcessor {
    int DEFAULT_MAX_CASES_TO_PROCESS = 100000;
    int DEFAULT_THREAD_LIMIT = 20;

    List<Long> migratedCases = new ArrayList<>();

    List<Long> failedCases = new ArrayList<>();

    default void validateCaseType(String caseType) {
        if (!StringUtils.hasText(caseType)) {
            throw new CaseMigrationException("Provide case type for the migration");
        }

        if (caseType.split(",").length > 1) {
            throw new CaseMigrationException("Only One case type at a time is allowed for the migration");
        }
    }

    default void assertLimitReached(Long id, int maxCasesToProcessLimit) {
        if (migratedCases.size() > maxCasesToProcessLimit) {
            String message = format("Stopping at case id %s as migration limit of %s configured is reached",
                id,
                maxCasesToProcessLimit);
            throw new MigrationLimitReachedException(message);
        }
    }

    void process(User user) throws InterruptedException;

    void migrateSingleCase(User user, String caseId);
}
