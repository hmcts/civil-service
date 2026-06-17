package uk.gov.hmcts.reform.civil.service.search.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provider that facilitates paginated searches against ElasticSearch using CCD's searchCases API.
 * This class uses the 'search_after' feature of ElasticSearch to allow efficient deep pagination.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticSearchPaginatedStreamProvider {

    private final CoreCaseDataService coreCaseDataService;

    /**
     * Gets a paginated search result using the default search_after key extractor (Case ID).
     *
     * @param queryProvider the provider for the ElasticSearch query
     * @param pageSize the number of results to fetch per page
     * @return an ElasticSearchResult containing the result stream and total count
     */
    public ElasticSearchResult getPaginatedSearchResult(PaginatedQueryProvider queryProvider, int pageSize) {
        return getPaginatedSearchResult(queryProvider, caseDetails -> PageToken.of(caseDetails.getId().toString()), pageSize);
    }

    /**
     * Gets a paginated search result using a custom search_after key extractor.
     *
     * @param queryProvider the provider for the ElasticSearch query
     * @param searchAfterKeyProvider the provider for extracting the search_after key from CaseDetails
     * @param pageSize the number of results to fetch per page
     * @return an ElasticSearchResult containing the result stream and total count
     */
    public ElasticSearchResult getPaginatedSearchResult(PaginatedQueryProvider queryProvider,
                                                        SearchAfterKeyProvider searchAfterKeyProvider,
                                                        int pageSize) {
        PaginatedQuery initialQuery = queryProvider.getPaginatedQuery(PageToken.initial(), pageSize);
        SearchResult searchResult = coreCaseDataService.searchCasesPaginated(initialQuery);

        int total = searchResult != null ? searchResult.getTotal() : 0;
        Stream<CaseDetails> stream = StreamSupport.stream(
            new ElasticSearchSpliterator(queryProvider, searchAfterKeyProvider, searchResult, pageSize),
            false
        );

        return new ElasticSearchResult(stream, total);
    }

    private class ElasticSearchSpliterator extends Spliterators.AbstractSpliterator<CaseDetails> {

        private final PaginatedQueryProvider queryProvider;
        private final SearchAfterKeyProvider searchAfterKeyProvider;
        private final int pageSize;
        private PageToken pageToken;
        private List<CaseDetails> currentCases = Collections.emptyList();
        private int currentCaseIndex = 0;
        private boolean hasMorePages = true;

        private int totalCasesFetched = 0;
        private int totalCasesAvailable = 0;

        protected ElasticSearchSpliterator(PaginatedQueryProvider queryProvider,
                                           SearchAfterKeyProvider searchAfterKeyProvider,
                                           SearchResult initialSearchResult,
                                           int pageSize) {
            super(Long.MAX_VALUE, Spliterator.ORDERED);
            this.queryProvider = queryProvider;
            this.searchAfterKeyProvider = searchAfterKeyProvider;
            this.pageSize = pageSize;
            this.pageToken = PageToken.initial();

            if (initialSearchResult != null) {
                this.totalCasesAvailable = initialSearchResult.getTotal();
                currentCases = initialSearchResult.getCases() != null
                    ? initialSearchResult.getCases()
                    : Collections.emptyList();

                if (!currentCases.isEmpty()) {
                    totalCasesFetched += currentCases.size();
                    pageToken = searchAfterKeyProvider.getSearchAfterKey(currentCases.getLast());
                    hasMorePages = totalCasesFetched < totalCasesAvailable;
                } else {
                    hasMorePages = false;
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super CaseDetails> action) {
            if (currentCaseIndex >= currentCases.size()) {
                if (!hasMorePages || !fetchNextPage()) {
                    return false;
                }
            }

            CaseDetails caseDetails = currentCases.get(currentCaseIndex++);
            action.accept(caseDetails);
            return true;
        }

        private boolean fetchNextPage() {
            log.debug("Fetching next page of cases with pageToken: {}", pageToken);
            PaginatedQuery paginatedQuery = queryProvider.getPaginatedQuery(pageToken, pageSize);
            SearchResult searchResult = coreCaseDataService.searchCasesPaginated(paginatedQuery);

            currentCases = searchResult != null && searchResult.getCases() != null
                ? searchResult.getCases()
                : Collections.emptyList();

            if (searchResult != null) {
                this.totalCasesAvailable = searchResult.getTotal();
                if (currentCases.size() > totalCasesAvailable) {
                    log.warn(
                        "Search result total ({}) is less than the number of cases returned ({}). "
                            + "This may indicate an inconsistency in the search results.",
                        totalCasesAvailable, currentCases.size()
                    );
                }
            }

            currentCaseIndex = 0;
            totalCasesFetched += currentCases.size();
            hasMorePages = totalCasesFetched < totalCasesAvailable;

            if (currentCases.isEmpty()) {
                log.debug("No more cases found, stopping pagination.");
                return false;
            }

            PageToken nextPageToken = searchAfterKeyProvider.getSearchAfterKey(currentCases.getLast());
            boolean hasProgressed = !nextPageToken.isInitial() && !nextPageToken.equals(pageToken);

            if (!hasProgressed) {
                log.warn(
                    "Pagination did not progress. Stopping to avoid infinite loop. pageToken: {}",
                    pageToken
                );
                hasMorePages = false;
                return false;
            }

            this.pageToken = nextPageToken;
            log.debug("Fetched {} cases. More pages available: {}", currentCases.size(), hasMorePages);
            return true;
        }
    }
}
