package uk.gov.hmcts.reform.civil.service.search.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticSearchPaginatedStreamProviderTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    private ElasticSearchPaginatedStreamProvider provider;

    @BeforeEach
    void setup() {
        provider = new ElasticSearchPaginatedStreamProvider(coreCaseDataService);
    }

    @Test
    void shouldPaginateCorrectlyForThreePagesUsingGetPaginatedSearchResult() {
        CaseDetails case1 = CaseDetails.builder().id(1L).build();
        CaseDetails case2 = CaseDetails.builder().id(2L).build();
        CaseDetails case3 = CaseDetails.builder().id(3L).build();

        SearchResult page1 = SearchResult.builder().total(3).cases(List.of(case1)).build();
        SearchResult page2 = SearchResult.builder().total(3).cases(List.of(case2)).build();
        SearchResult page3 = SearchResult.builder().total(3).cases(List.of(case3)).build();

        when(coreCaseDataService.searchCasesPaginated(any(PaginatedQuery.class)))
            .thenAnswer(invocation -> {
                PaginatedQuery query = invocation.getArgument(0);
                if (query.isInitialSearch()) {
                    return page1;
                } else if ("1".equals(query.getSearchAfterValue())) {
                    return page2;
                } else if ("2".equals(query.getSearchAfterValue())) {
                    return page3;
                }
                return SearchResult.builder().total(0).cases(List.of()).build();
            });

        int pageSize = 1;
        PaginatedQueryProvider queryProvider = (pageToken, ps) -> new PaginatedQuery(
            null,
            null,
            0,
            pageToken,
            ps
        );

        ElasticSearchResult result = provider.getPaginatedSearchResult(
            queryProvider,
            pageSize
        );

        assertThat(result.totalResults()).isEqualTo(3);
        List<CaseDetails> cases = result.caseDetailsStream().toList();

        assertThat(cases).hasSize(3);
        assertThat(cases.get(0).getId()).isEqualTo(1L);
        assertThat(cases.get(1).getId()).isEqualTo(2L);
        assertThat(cases.get(2).getId()).isEqualTo(3L);

        verify(coreCaseDataService, times(3)).searchCasesPaginated(any());
    }

    @Test
    void shouldHandleEmptyResults() {
        SearchResult emptyResult = SearchResult.builder().total(0).cases(Collections.emptyList()).build();
        when(coreCaseDataService.searchCasesPaginated(any())).thenReturn(emptyResult);

        int pageSize = 10;
        PaginatedQueryProvider queryProvider = (pageToken, ps) -> new PaginatedQuery(
            null,
            null,
            0,
            pageToken,
            ps
        );
        ElasticSearchResult result = provider.getPaginatedSearchResult(
            queryProvider,
            pageSize
        );

        assertThat(result.totalResults()).isEqualTo(0);
        assertThat(result.caseDetailsStream().toList()).isEmpty();
    }

    @Test
    void shouldHandleNullSearchResult() {
        when(coreCaseDataService.searchCasesPaginated(any())).thenReturn(null);

        int pageSize = 10;
        PaginatedQueryProvider queryProvider = (pageToken, ps) -> new PaginatedQuery(
            null,
            null,
            0,
            pageToken,
            ps
        );

        ElasticSearchResult result = provider.getPaginatedSearchResult(
            queryProvider,
            pageSize
        );

        assertThat(result.totalResults()).isEqualTo(0);
        assertThat(result.caseDetailsStream().toList()).isEmpty();
    }

    @Test
    void shouldHandleNullCasesInSearchResult() {
        SearchResult searchResult = SearchResult.builder().total(10).cases(null).build();
        when(coreCaseDataService.searchCasesPaginated(any())).thenReturn(searchResult);

        int pageSize = 10;
        PaginatedQueryProvider queryProvider = (pageToken, ps) -> new PaginatedQuery(
            null,
            null,
            0,
            pageToken,
            ps
        );

        ElasticSearchResult result = provider.getPaginatedSearchResult(
            queryProvider,
            pageSize
        );

        assertThat(result.totalResults()).isEqualTo(10);
        assertThat(result.caseDetailsStream().toList()).isEmpty();
    }

    @Test
    void shouldStopIfPaginationStallsToAvoidInfiniteLoop() {
        CaseDetails case1 = CaseDetails.builder().id(1L).build();
        // Return same case twice, so sortKey doesn't change
        SearchResult page = SearchResult.builder().total(10).cases(List.of(case1)).build();

        when(coreCaseDataService.searchCasesPaginated(any())).thenReturn(page);

        int pageSize = 1;
        PaginatedQueryProvider queryProvider = (pageToken, ps) -> new PaginatedQuery(
            null,
            null,
            0,
            pageToken,
            ps
        );

        ElasticSearchResult result = provider.getPaginatedSearchResult(
            queryProvider,
            pageSize
        );

        List<CaseDetails> cases = result.caseDetailsStream().toList();

        // Should only have 1 case because pagination stalled
        assertThat(cases).hasSize(1);
        verify(coreCaseDataService, times(2)).searchCasesPaginated(any());
    }

    @Test
    void shouldHandleCustomSortKeyExtractor() {
        Map<String, Object> data1 = new HashMap<>();
        data1.put("customKey", "AAA");
        CaseDetails case1 = CaseDetails.builder().id(1L).data(data1).build();

        Map<String, Object> data2 = new HashMap<>();
        data2.put("customKey", "BBB");
        CaseDetails case2 = CaseDetails.builder().id(2L).data(data2).build();

        SearchResult page1 = SearchResult.builder().total(2).cases(List.of(case1)).build();
        SearchResult page2 = SearchResult.builder().total(2).cases(List.of(case2)).build();

        when(coreCaseDataService.searchCasesPaginated(any(PaginatedQuery.class)))
            .thenAnswer(invocation -> {
                PaginatedQuery query = invocation.getArgument(0);
                if (query.isInitialSearch()) {
                    return page1;
                } else if ("AAA".equals(query.getSearchAfterValue())) {
                    return page2;
                }
                return SearchResult.builder().total(0).cases(List.of()).build();
            });

        int pageSize = 1;
        PaginatedQueryProvider queryProvider = (pageToken, ps) -> new PaginatedQuery(
            null,
            null,
            0,
            pageToken,
            ps
        );
        SearchAfterKeyProvider sortKeyExtractor = caseDetails -> PageToken.of((String) caseDetails.getData().get("customKey"));

        ElasticSearchResult result = provider.getPaginatedSearchResult(
            queryProvider,
            sortKeyExtractor,
            pageSize
        );

        List<CaseDetails> cases = result.caseDetailsStream().toList();

        assertThat(cases).hasSize(2);
        assertThat(cases.get(0).getId()).isEqualTo(1L);
        assertThat(cases.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void shouldThrowExceptionWhenInitialSearchFails() {
        when(coreCaseDataService.searchCasesPaginated(any())).thenThrow(new RuntimeException("API Error"));

        int pageSize = 10;
        PaginatedQueryProvider queryProvider = (pageToken, ps) -> new PaginatedQuery(
            null,
            null,
            0,
            pageToken,
            ps
        );

        assertThatThrownBy(() -> provider.getPaginatedSearchResult(queryProvider, pageSize))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to fetch initial page from ElasticSearch");
    }

    @Test
    void shouldHandleExceptionDuringBackgroundPagination() {
        CaseDetails case1 = CaseDetails.builder().id(1L).build();
        SearchResult page1 = SearchResult.builder().total(2).cases(List.of(case1)).build();

        when(coreCaseDataService.searchCasesPaginated(any(PaginatedQuery.class)))
            .thenAnswer(invocation -> {
                PaginatedQuery query = invocation.getArgument(0);
                if (query.isInitialSearch()) {
                    return page1;
                }
                throw new RuntimeException("Background API Error");
            });

        int pageSize = 1;
        PaginatedQueryProvider queryProvider = (pageToken, ps) -> new PaginatedQuery(
            null,
            null,
            0,
            pageToken,
            ps
        );

        ElasticSearchResult result = provider.getPaginatedSearchResult(queryProvider, pageSize);
        List<CaseDetails> cases = result.caseDetailsStream().toList();

        // Should return first page cases and then stop
        assertThat(cases).hasSize(1);
        assertThat(cases.get(0).getId()).isEqualTo(1L);
        verify(coreCaseDataService, times(2)).searchCasesPaginated(any());
    }

    @Test
    void shouldCallOnCloseHandlerWhenStreamIsClosed() {
        SearchResult searchResult = SearchResult.builder().total(0).cases(Collections.emptyList()).build();
        when(coreCaseDataService.searchCasesPaginated(any())).thenReturn(searchResult);

        int pageSize = 10;
        PaginatedQueryProvider queryProvider = (pageToken, ps) -> new PaginatedQuery(
            null,
            null,
            0,
            pageToken,
            ps
        );

        ElasticSearchResult result = provider.getPaginatedSearchResult(queryProvider, pageSize);
        boolean[] closed = {false};
        try (Stream<CaseDetails> stream = result.caseDetailsStream()) {
            stream.onClose(() -> closed[0] = true);
        }

        assertThat(closed[0]).isTrue();
    }
}
