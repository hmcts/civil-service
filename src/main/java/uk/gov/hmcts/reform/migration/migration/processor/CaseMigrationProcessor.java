package uk.gov.hmcts.reform.migration.migration.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.migration.domain.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.migration.domain.exception.MigrationLimitReachedException;
import uk.gov.hmcts.reform.migration.migration.MigrationProperties;
import uk.gov.hmcts.reform.migration.migration.ccd.MigrationCoreCaseDataService;
import uk.gov.hmcts.reform.migration.migration.service.DataMigrationService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "migration", name = "esEnabled", havingValue = "false")
public class CaseMigrationProcessor implements MigrationProcessor {
    private final MigrationCoreCaseDataService coreCaseDataService;
    private final DataMigrationService<Map<String, Object>> dataMigrationService;
    private final MigrationProperties migrationProperties;

    @Override
    public void process(User user) throws InterruptedException {
        int numberOfThreads = ofNullable(migrationProperties.getNumThreads()).orElse(DEFAULT_THREAD_LIMIT);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        try {
            String authToken = user.getAuthToken();
            String userId = user.getUserDetails().getId();
            int numberOfPages = coreCaseDataService.getNumberOfPages(authToken, userId, new HashMap<>());
            log.info("Total no of pages: {}", numberOfPages);

            IntStream.rangeClosed(1, numberOfPages).boxed()
                .peek(pageNo ->
                    log.info("Fetching cases for the page no {} of total {}", pageNo, numberOfPages)
                )
                .flatMap(pageNumber -> coreCaseDataService.fetchPage(authToken, userId, pageNumber).stream())
                .filter(dataMigrationService.accepts())
                .forEach(submitMigration(user, executorService));

        } catch (MigrationLimitReachedException ex) {
            throw ex;
        } finally {
            log.info("Shutting down executorService");
            executorService.shutdown();
            executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        }
    }

    private Consumer<CaseDetails> submitMigration(User user, ExecutorService executorService) {
        return caseDetails -> {
            log.info("Submitting task for migration of case  {}.", caseDetails.getId());
            executorService.submit(() -> updateCase(user, caseDetails));
        };
    }

    public void migrateSingleCase(User user, String caseId) {
        try {
            validateCaseType(migrationProperties.getCaseType());
            String userToken = user.getAuthToken();
            CaseDetails caseDetails = coreCaseDataService.fetchOne(
                userToken,
                caseId
            ).orElseThrow(CaseNotFoundException::new);
            if (dataMigrationService.accepts().test(caseDetails)) {
                updateCase(user, caseDetails);
            } else {
                log.info("Case {} already migrated", caseDetails.getId());
            }
        } catch (CaseNotFoundException ex) {
            log.error("Case {} not found due to: {}", caseId, ex.getMessage());
        } catch (MigrationLimitReachedException ex) {
            throw ex;
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }
    }

    private void updateCase(User user, CaseDetails caseDetails) {
        Long id = caseDetails.getId();
        log.info("Updating case {}", id);
        int maxCasesToProcessLimit = ofNullable(migrationProperties.getMaxCasesToProcess())
            .orElse(DEFAULT_MAX_CASES_TO_PROCESS);
        assertLimitReached(id, maxCasesToProcessLimit);
        try {
            log.debug("Case data: {}", caseDetails.getData());
            if (!migrationProperties.isDryRun()) {
                coreCaseDataService.update(
                    user,
                    caseDetails,
                    migrationProperties.getCaseType(),
                    migrationProperties.getEventId(),
                    migrationProperties.getEventSummary(),
                    migrationProperties.getEventDescription(),
                    dataMigrationService.migrate(caseDetails)
                );
                log.info("Case {} successfully updated", id);
            } else {
                log.info("Case {} dry run migration", id);
            }
            migratedCases.add(id);
        } catch (Exception e) {
            log.error("Case {} update failed due to: {}", id, e.getMessage());
            failedCases.add(id);
        }
    }
}
