package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
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
public class ElasticSearchPaginatedStreamProvider {

    private final CoreCaseDataService coreCaseDataService;

    public Stream<CaseDetails> getPaginatedStream(Function<String, PaginatedQuery> queryFunction) {
        return getPaginatedStream(queryFunction, caseDetails -> caseDetails.getId().toString());
    }

    public Stream<CaseDetails> getPaginatedStream(Function<String, PaginatedQuery> queryFunction,
                                                  Function<CaseDetails, String> sortKeyExtractor) {
        return StreamSupport.stream(
            new Spliterators.AbstractSpliterator<>(Long.MAX_VALUE, Spliterator.ORDERED) {
                private String searchAfterValue;
                private List<CaseDetails> currentCases = Collections.emptyList();
                private int currentCaseIndex = 0;

                @Override
                public boolean tryAdvance(Consumer<? super CaseDetails> action) {
                    if (currentCaseIndex >= currentCases.size()) {
                        if (!fetchNextPage()) {
                            return false;
                        }
                    }

                    CaseDetails caseDetails = currentCases.get(currentCaseIndex++);
                    searchAfterValue = sortKeyExtractor.apply(caseDetails);
                    action.accept(caseDetails);
                    return true;
                }

                private boolean fetchNextPage() {
                    SearchResult searchResult = coreCaseDataService.searchCasesPaginated(queryFunction.apply(searchAfterValue));
                    currentCases = searchResult != null && searchResult.getCases() != null
                        ? searchResult.getCases()
                        : Collections.emptyList();
                    currentCaseIndex = 0;
                    if (currentCases.isEmpty()) {
                        return false;
                    }

                    String nextSearchAfterValue = sortKeyExtractor.apply(currentCases.getLast());
                    return nextSearchAfterValue != null && !nextSearchAfterValue.equals(searchAfterValue);
                }
            }, false
        );
    }
}
