package uk.gov.hmcts.reform.migration.migration.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.migration.domain.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.migration.domain.exception.MigrationLimitReachedException;
import uk.gov.hmcts.reform.migration.migration.MigrationProperties;
import uk.gov.hmcts.reform.migration.migration.ccd.MigrationCoreCaseDataService;
import uk.gov.hmcts.reform.migration.migration.query.ElasticSearchQuery;
import uk.gov.hmcts.reform.migration.migration.service.DataMigrationService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "migration", name = "esEnabled", havingValue = "true")
public class CaseMigrationProcessorES implements MigrationProcessor {

    private final AuthTokenGenerator authTokenGenerator;
    private final DataMigrationService<Map<String, Object>> dataMigrationService;
    private final MigrationCoreCaseDataService coreCaseDataService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final MigrationProperties migrationProperties;

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
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }
    }

    @Override
    public void process(User user) throws InterruptedException {
        try {
            String caseType = migrationProperties.getCaseType();
            validateCaseType(caseType);
            log.info("Data migration of cases started for case type: {}", caseType);
            int numberOfThreads = ofNullable(migrationProperties.getNumThreads()).orElse(DEFAULT_THREAD_LIMIT);
            String authToken = authTokenGenerator.generate();
            int querySize = ofNullable(migrationProperties.getEsQuerySize()).orElse(100);

            SearchResult searchResult = fetchFirstPage(user.getAuthToken(), authToken, caseType, querySize);
            if (searchResult != null && searchResult.getTotal() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

                List<CaseDetails> searchResultCases = searchResult.getCases();
                searchResultCases
                    .stream()
                    .forEach(submitMigration(user, executorService));
                String searchAfterValue = searchResultCases.get(searchResultCases.size() - 1).getId().toString();

                boolean keepSearching;
                do {
                    SearchResult subsequentSearchResult = fetchNextPage(user.getAuthToken(),
                        authToken,
                        caseType,
                        searchAfterValue,
                        querySize);

                    keepSearching = false;
                    if (subsequentSearchResult != null) {
                        List<CaseDetails> subsequentSearchResultCases = subsequentSearchResult.getCases();
                        subsequentSearchResultCases
                            .stream()
                            .forEach(submitMigration(user, executorService));
                        keepSearching = subsequentSearchResultCases.size() > 0;
                        if (keepSearching) {
                            searchAfterValue = subsequentSearchResultCases
                                .get(subsequentSearchResultCases.size() - 1).getId().toString();
                        }
                    }
                } while (keepSearching);

                executorService.shutdown();
                executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
            }
        } catch (MigrationLimitReachedException ex) {
            throw ex;
        }
    }

    private Consumer<CaseDetails> submitMigration(User user, ExecutorService executorService) {
        return caseDetails -> {
            log.info("Submitting task for migration of case  {}.", caseDetails.getId());
            executorService.submit(() -> updateCase(user, caseDetails));
        };
    }

    public SearchResult fetchFirstPage(String userToken, String authToken, String caseType, int querySize) {
        ElasticSearchQuery elasticSearchQuery = ElasticSearchQuery.builder()
            .initialSearch(true)
            .size(querySize)
            .build();
        log.info("Fetching the case details from elastic search for case type {}.", caseType);
        return coreCaseDataApi.searchCases(userToken,
            authToken,
            caseType, elasticSearchQuery.getQuery()
        );
    }

    public SearchResult fetchNextPage(String userToken,
                                      String authToken,
                                      String caseType,
                                      String searchAfterValue,
                                      int querySize) {


        ElasticSearchQuery subsequentElasticSearchQuery = ElasticSearchQuery.builder()
            .initialSearch(false)
            .size(querySize)
            .searchAfterValue(searchAfterValue)
            .build();

        SearchResult subsequentSearchResult =
            coreCaseDataApi.searchCases(userToken,
                authToken,
                caseType, subsequentElasticSearchQuery.getQuery()
            );
        return subsequentSearchResult;
    }

    private void updateCase(User user, CaseDetails caseDetails) {
        Long id = caseDetails.getId();
        log.info("Updating case {}", caseDetails.getId());
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
