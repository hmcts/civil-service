package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticSearchPaginatedStreamProvider {

    private final CoreCaseDataService coreCaseDataService;

    public Stream<CaseDetails> getPaginatedStream(Function<String, PaginatedQuery> queryFunction) {
        return getPaginatedStream(queryFunction, caseDetails -> caseDetails.getId().toString());
    }

    public Stream<CaseDetails> getPaginatedStream(Function<String, PaginatedQuery> queryFunction,
                                                  Function<CaseDetails, String> sortKeyExtractor) {
        return StreamSupport.stream(new ElasticSearchSpliterator(queryFunction, sortKeyExtractor), false);
    }

    private class ElasticSearchSpliterator extends Spliterators.AbstractSpliterator<CaseDetails> {
        private final Function<String, PaginatedQuery> queryFunction;
        private final Function<CaseDetails, String> sortKeyExtractor;
        private String searchAfterValue;
        private List<CaseDetails> currentCases = Collections.emptyList();
        private int currentCaseIndex = 0;
        private boolean hasMorePages = true;

        protected ElasticSearchSpliterator(Function<String, PaginatedQuery> queryFunction,
                                         Function<CaseDetails, String> sortKeyExtractor) {
            super(Long.MAX_VALUE, Spliterator.ORDERED);
            this.queryFunction = queryFunction;
            this.sortKeyExtractor = sortKeyExtractor;
        }

        @Override
        public boolean tryAdvance(Consumer<? super CaseDetails> action) {
            if (currentCaseIndex >= currentCases.size()) {
                if (!hasMorePages || !fetchNextPage()) {
                    return false;
                }
            }

            CaseDetails caseDetails = currentCases.get(currentCaseIndex++);
            searchAfterValue = sortKeyExtractor.apply(caseDetails);
            action.accept(caseDetails);
            return true;
        }

        private boolean fetchNextPage() {
            log.debug("Fetching next page of cases with searchAfterValue: {}", searchAfterValue);
            PaginatedQuery paginatedQuery = queryFunction.apply(searchAfterValue);
            SearchResult searchResult = coreCaseDataService.searchCasesPaginated(paginatedQuery);

            currentCases = searchResult != null && searchResult.getCases() != null
                ? searchResult.getCases()
                : Collections.emptyList();
            currentCaseIndex = 0;
            hasMorePages = currentCases.size() == paginatedQuery.getPageSize();

            if (currentCases.isEmpty()) {
                log.debug("No more cases found, stopping pagination.");
                return false;
            }

            String nextSearchAfterValue = sortKeyExtractor.apply(currentCases.getLast());
            boolean hasProgressed = nextSearchAfterValue != null && !nextSearchAfterValue.equals(searchAfterValue);

            if (!hasProgressed) {
                log.warn("Pagination did not progress. Stopping to avoid infinite loop. searchAfterValue: {}", searchAfterValue);
                hasMorePages = false;
                return false;
            }

            log.debug("Fetched {} cases. More pages available: {}", currentCases.size(), hasMorePages);
            return true;
        }
    }
}
