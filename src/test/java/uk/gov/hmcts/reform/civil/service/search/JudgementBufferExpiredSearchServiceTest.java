package uk.gov.hmcts.reform.civil.service.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JudgementBufferExpiredSearchServiceTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    private ElasticSearchPaginatedStreamProvider elasticSearchPaginatedStreamProvider;
    private JudgementBufferExpiredSearchService searchService;

    @Captor
    private ArgumentCaptor<PaginatedQuery> queryCaptor;

    @BeforeEach
    void setup() {
        elasticSearchPaginatedStreamProvider = new ElasticSearchPaginatedStreamProvider(coreCaseDataService);
        searchService = new JudgementBufferExpiredSearchService(elasticSearchPaginatedStreamProvider);
    }

    @Test
    void shouldStopPaginatingIfPageIsNotFull() {
        // Given
        CaseDetails case1 = CaseDetails.builder().id(1L).build();
        CaseDetails case2 = CaseDetails.builder().id(2L).build();

        // Page size is 50, but we return 2 cases. This should be the last page.
        SearchResult page1 = SearchResult.builder().total(2).cases(List.of(case1, case2)).build();

        when(coreCaseDataService.searchCasesPaginated(any())).thenReturn(page1);

        // When
        Stream<CaseDetails> casesStream = searchService.getCasesStream();
        List<CaseDetails> allCases = casesStream.toList();

        // Then
        assertThat(allCases).hasSize(2);
        // Should only call once because the first page was not full
        verify(coreCaseDataService, times(1)).searchCasesPaginated(any());
    }

    @Test
    void shouldReturnStreamThatPaginatesCorrectly() {
        // Given
        CaseDetails case1 = CaseDetails.builder().id(1L).build();
        CaseDetails case2 = CaseDetails.builder().id(2L).build();
        CaseDetails case3 = CaseDetails.builder().id(3L).build();
        CaseDetails case4 = CaseDetails.builder().id(4L).build();
        CaseDetails case5 = CaseDetails.builder().id(5L).build();
        CaseDetails case6 = CaseDetails.builder().id(6L).build();
        CaseDetails case7 = CaseDetails.builder().id(7L).build();
        CaseDetails case8 = CaseDetails.builder().id(8L).build();
        CaseDetails case9 = CaseDetails.builder().id(9L).build();
        CaseDetails case10 = CaseDetails.builder().id(10L).build();
        CaseDetails case11 = CaseDetails.builder().id(11L).build();

        // Page 1 - returning full page (size 50 by default in JudgementBufferExpiredSearchService)
        // For testing we will return 10 cases but the test logic in ElasticSearchPaginatedStreamProvider
        // will think it's full IF we set the page size to 10 in the query.
        // Wait, JudgementBufferExpiredSearchService sets page size to 50.
        // If I want to test pagination with a full page, I should either:
        // 1. Mock the first page to have 50 cases.
        // 2. Or, acknowledge that if I return 10 cases, and page size is 50, it will stop.

        // Let's modify the test to reflect the new behavior:
        // If the first page has 10 cases and page size is 50, it SHOULD stop.
        // To test pagination, I need the first page to be full (50 cases).

        // Alternatively, I can change the test to use a smaller page size if I could control it,
        // but it's hardcoded to 50 in JudgementBufferExpiredSearchService.

        // Let's create 50 cases for the first page.
        List<CaseDetails> fiftyCases = java.util.stream.IntStream.rangeClosed(1, 50)
            .mapToObj(i -> CaseDetails.builder().id((long) i).build())
            .toList();
        CaseDetails case51 = CaseDetails.builder().id(51L).build();

        SearchResult page1 = SearchResult.builder()
            .total(51)
            .cases(fiftyCases)
            .build();
        SearchResult page2 = SearchResult.builder()
            .total(51)
            .cases(List.of(case51))
            .build();

        when(coreCaseDataService.searchCasesPaginated(any(PaginatedQuery.class)))
            .thenReturn(page1)
            .thenReturn(page2);

        // When
        Stream<CaseDetails> casesStream = searchService.getCasesStream();
        List<CaseDetails> allCases = casesStream.toList();

        // Then
        assertThat(allCases).hasSize(51);
        verify(coreCaseDataService, times(2)).searchCasesPaginated(queryCaptor.capture());
        List<PaginatedQuery> capturedQueries = queryCaptor.getAllValues();

        assertThat(capturedQueries.get(0).toString()).contains("\"size\": 50");
        assertThat(capturedQueries.get(1).toString()).contains("\"search_after\": [\"50\"]");
    }
}
