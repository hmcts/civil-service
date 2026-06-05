package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class ElasticSearchPaginatedStreamProvider {

    private static final int ES_DEFAULT_SEARCH_LIMIT = 10;
    private final CoreCaseDataService coreCaseDataService;

    public Stream<CaseDetails> getPaginatedStream(Function<String, PaginatedQuery> queryFunction) {
        return StreamSupport.stream(
            new Spliterators.AbstractSpliterator<>(Long.MAX_VALUE, Spliterator.ORDERED) {
                private String searchAfterValue;
                private int currentCaseIndex = 0;
                private SearchResult currentSearchResult;

                @Override
                public boolean tryAdvance(Consumer<? super CaseDetails> action) {
                    if (currentSearchResult == null || currentCaseIndex >= currentSearchResult.getCases().size()) {
                        if (currentSearchResult != null && currentSearchResult.getCases().size() < ES_DEFAULT_SEARCH_LIMIT) {
                            return false;
                        }
                        fetchNextPage();
                    }

                    if (currentSearchResult.getCases().isEmpty() || currentCaseIndex >= currentSearchResult.getCases().size()) {
                        return false;
                    }

                    CaseDetails caseDetails = currentSearchResult.getCases().get(currentCaseIndex++);
                    searchAfterValue = caseDetails.getId().toString();
                    action.accept(caseDetails);
                    return true;
                }

                private void fetchNextPage() {
                    currentSearchResult = coreCaseDataService.searchCasesPaginated(queryFunction.apply(searchAfterValue));
                    currentCaseIndex = 0;
                }
            }, false
        );
    }
}
