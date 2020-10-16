package uk.gov.hmcts.reform.unspec.service.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.unspec.model.search.Query;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
abstract class ElasticSearchServiceTest {

    @Captor
    private ArgumentCaptor<Query> queryCaptor;

    @Mock
    protected CoreCaseDataService coreCaseDataService;

    protected ElasticSearchService searchService;

    @Test
    void shouldCallGetCasesOnce_WhenCasesReturnEqualsTotalCases() {
        SearchResult searchResult = buildSearchResultWithTotalCases(1);

        when(coreCaseDataService.searchCases(any())).thenReturn(searchResult);

        assertThat(searchService.getCases()).isEqualTo(searchResult.getCases());
        verify(coreCaseDataService).searchCases(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).isEqualToComparingFieldByField(buildQuery(0));
    }

    @Test
    void shouldCallGetCasesOnce_WhenNoCasesReturned() {
        SearchResult searchResult = buildSearchResult(0, emptyList());

        when(coreCaseDataService.searchCases(any())).thenReturn(searchResult);

        assertThat(searchService.getCases()).isEmpty();
        verify(coreCaseDataService).searchCases(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).isEqualToComparingFieldByField(buildQuery(0));
    }

    @Test
    void shouldCallGetCasesOnce_WhenCasesRetrievedEqualsEsSearchLimit() {
        SearchResult searchResult = buildSearchResultWithTotalCases(10);

        when(coreCaseDataService.searchCases(any())).thenReturn(searchResult);

        assertThat(searchService.getCases()).hasSize(1);
        verify(coreCaseDataService).searchCases(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).isEqualToComparingFieldByField(buildQuery(0));
    }

    @Test
    void shouldCallGetCasesMultipleTimes_WhenCasesReturnedIsMoreThanEsSearchLimit() {
        SearchResult searchResult = buildSearchResultWithTotalCases(11);

        when(coreCaseDataService.searchCases(any())).thenReturn(searchResult);

        assertThat(searchService.getCases()).hasSize(2);
        verify(coreCaseDataService, times(2)).searchCases(queryCaptor.capture());

        List<Query> capturedQueries = queryCaptor.getAllValues();
        assertThat(capturedQueries.get(0)).isEqualToComparingFieldByField(buildQuery(0));
        assertThat(capturedQueries.get(1)).isEqualToComparingFieldByField(buildQuery(10));
    }

    private SearchResult buildSearchResultWithTotalCases(int i) {
        return buildSearchResult(i, List.of(CaseDetails.builder().id(1L).build()));
    }

    private SearchResult buildSearchResult(int i, List<CaseDetails> caseDetails) {
        return SearchResult.builder()
            .total(i)
            .cases(caseDetails)
            .build();
    }

    protected abstract Query buildQuery(int fromValue);
}
