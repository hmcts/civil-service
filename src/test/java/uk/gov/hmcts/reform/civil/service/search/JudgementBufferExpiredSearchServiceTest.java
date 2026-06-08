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

        // Page 1
        SearchResult page1 = SearchResult.builder()
            .total(11)
            .cases(List.of(case1, case2, case3, case4, case5, case6, case7, case8, case9, case10))
            .build();
        // Page 2
        SearchResult page2 = SearchResult.builder()
            .total(11)
            .cases(List.of(case11))
            .build();

        when(coreCaseDataService.searchCasesPaginated(any(PaginatedQuery.class)))
            .thenReturn(page1)
            .thenReturn(page2)
            .thenReturn(SearchResult.builder().cases(List.of()).build());

        // When
        Stream<CaseDetails> casesStream = searchService.getCasesStream();
        List<CaseDetails> allCases = casesStream.toList();

        // Then
        assertThat(allCases).hasSize(11);
        assertThat(allCases).containsExactly(case1, case2, case3, case4, case5, case6, case7, case8, case9, case10, case11);

        verify(coreCaseDataService, times(3)).searchCasesPaginated(queryCaptor.capture());
        List<PaginatedQuery> capturedQueries = queryCaptor.getAllValues();

        assertThat(capturedQueries.get(0).toString()).contains("\"from\": 0");
        assertThat(capturedQueries.get(0).toString()).contains("\"size\": 50");
        assertThat(capturedQueries.get(0).toString()).contains("\"id\": \"asc\"");
        assertThat(capturedQueries.get(1).toString()).contains("\"search_after\": [\"10\"]");
        assertThat(capturedQueries.get(1).toString()).contains("\"from\": 0");
        assertThat(capturedQueries.get(1).toString()).contains("\"size\": 50");
        assertThat(capturedQueries.get(1).toString()).contains("\"id\": \"asc\"");
    }
}
