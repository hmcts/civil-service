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
     * <p>
     * Note: The resulting {@link Stream} is NOT thread-safe for concurrent consumption as it relies
     * on a stateful {@link Spliterator} for pagination.
     * </p>
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
     * <p>
     * Note: The resulting {@link Stream} is NOT thread-safe for concurrent consumption as it relies
     * on a stateful {@link Spliterator} for pagination.
     * </p>
     *
     * <p>
     * IMPORTANT: The field returned by the {@code searchAfterKeyProvider} MUST match the 'sort' field
     * used in the {@link PaginatedQuery} returned by the {@code queryProvider}.
     * </p>
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
        SearchResult searchResult;
        try {
            searchResult = coreCaseDataService.searchCasesPaginated(initialQuery);
        } catch (Exception e) {
            log.error("Error during initial ElasticSearch paginated search. Query: {}, PageSize: {}",
                      initialQuery, pageSize, e);
            throw new RuntimeException("Failed to fetch initial page from ElasticSearch", e);
        }

        int total = searchResult != null ? searchResult.getTotal() : 0;
        Stream<CaseDetails> stream = StreamSupport.stream(
            new ElasticSearchSpliterator(queryProvider, searchAfterKeyProvider, searchResult, pageSize),
            false
        ).onClose(() -> log.debug("ElasticSearch pagination stream closed"));

        return new ElasticSearchResult(stream, total);
    }

    /**
     * A stateful {@link Spliterator} that handles the lazy fetching of pages from ElasticSearch.
     * It manages the current buffer of cases and triggers the next API call when the buffer is exhausted.
     *
     * <p>
     * This implementation is NOT thread-safe.
     * </p>
     */
    private class ElasticSearchSpliterator extends Spliterators.AbstractSpliterator<CaseDetails> {

        private final PaginatedQueryProvider queryProvider;
        private final SearchAfterKeyProvider searchAfterKeyProvider;
        private final int pageSize;
        private PageToken pageToken;
        private List<CaseDetails> currentCases = Collections.emptyList();
        private int currentCaseIndex = 0;
        private boolean hasMorePages = false;

        private int totalCasesFetched = 0;
        private int expectedTotalCases = 0;

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
                this.expectedTotalCases = initialSearchResult.getTotal();
                currentCases = initialSearchResult.getCases() != null
                    ? initialSearchResult.getCases()
                    : Collections.emptyList();

                if (!currentCases.isEmpty()) {
                    totalCasesFetched += currentCases.size();
                    pageToken = searchAfterKeyProvider.getSearchAfterKey(currentCases.getLast());
                    hasMorePages = totalCasesFetched < expectedTotalCases;
                } else {
                    hasMorePages = false;
                }
            }
        }

        /**
         * Attempts to advance to the next element in the stream.
         * If the current page buffer is empty, it attempts to fetch the next page.
         *
         * @param action The action to be performed for the next element
         * @return true if a next element existed and the action was performed, false otherwise
         */
        @Override
        public boolean tryAdvance(Consumer<? super CaseDetails> action) {
            boolean isEndOfPage = currentCaseIndex >= currentCases.size();

            if (isEndOfPage) {
                if (!hasMorePages || !fetchNextPage()) {
                    return false;
                }
            }

            CaseDetails caseDetails = currentCases.get(currentCaseIndex++);
            action.accept(caseDetails);
            return true;
        }

        /**
         * Fetches the next page of results from ElasticSearch.
         *
         * <p>
         * It performs the following:
         * 1. Constructs a {@link PaginatedQuery} using the current {@code pageToken}.
         * 2. Calls {@link CoreCaseDataService#searchCasesPaginated(PaginatedQuery)}.
         * 3. Updates the internal buffer {@code currentCases} and progress metrics.
         * 4. Extracts the new {@code pageToken} from the last item of the new page.
         * 5. Performs an infinite loop check to ensure the {@code pageToken} has changed.
         * </p>
         *
         * @return true if the next page was successfully fetched and contains data, false otherwise.
         */
        private boolean fetchNextPage() {
            PaginatedQuery paginatedQuery = queryProvider.getPaginatedQuery(pageToken, pageSize);

            SearchResult searchResult;
            try {
                searchResult = coreCaseDataService.searchCasesPaginated(paginatedQuery);
            } catch (Exception e) {
                log.error("Error during ElasticSearch paginated search. PageToken: {}, PageSize: {}, "
                              + "TotalCasesFetched: {}, ExpectedTotalCases: {}",
                          pageToken, pageSize, totalCasesFetched, expectedTotalCases, e);
                hasMorePages = false;
                return false;
            }

            currentCases = searchResult != null && searchResult.getCases() != null
                ? searchResult.getCases()
                : Collections.emptyList();

            if (searchResult != null) {
                if (totalCasesFetched + currentCases.size() > expectedTotalCases) {
                    log.warn(
                        "Total cases fetched ({}) exceeds expected total ({}) after adding current page ({}). "
                            + "This may indicate that more cases now match the query than during the initial search.",
                        totalCasesFetched, expectedTotalCases, currentCases.size()
                    );
                }
            }

            currentCaseIndex = 0;
            totalCasesFetched += currentCases.size();
            hasMorePages = totalCasesFetched < expectedTotalCases;

            if (currentCases.isEmpty()) {
                return false;
            }

            PageToken nextPageToken = searchAfterKeyProvider.getSearchAfterKey(currentCases.getLast());
            boolean hasProgressed = !nextPageToken.isInitial() && !nextPageToken.equals(pageToken);

            if (!hasProgressed) {
                log.warn(
                    "Pagination did not progress. Stopping to avoid infinite loop. "
                        + "pageToken: {}, pageSize: {}, totalCasesFetched: {}, expectedTotalCases: {}",
                    pageToken, pageSize, totalCasesFetched, expectedTotalCases
                );
                hasMorePages = false;
                return false;
            }

            this.pageToken = nextPageToken;
            return true;
        }
    }
}
