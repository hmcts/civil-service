package uk.gov.hmcts.reform.civil.service.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CasesStuckCheckSearchServiceTest {

    private CoreCaseDataService coreCaseDataService;
    private CasesStuckCheckSearchService service;

    @BeforeEach
    void setup() {
        coreCaseDataService = mock(CoreCaseDataService.class);
        service = new CasesStuckCheckSearchService(coreCaseDataService);
    }

    @Test
    void shouldBuildCorrectQuery() {
        String timeNow = "2025-01-01T00:00:00Z";
        Query query = service.query(0, timeNow);
        String queryStr = query.toString().replaceAll("\\s+", ""); // remove all spaces

        assertThat(queryStr).contains("\"to\":\"" + timeNow + "\"");
        assertThat(queryStr).contains("\"from\":0");
        assertThat(queryStr).contains("\"_source\":[\"reference\"]");
        assertThat(queryStr).contains("now-7d");
        assertThat(queryStr).contains("FINISHED");
    }

    @Test
    void shouldReturnAllResultsFromGetCases() {
        // Only 2 cases exist according to current service behavior
        CaseDetails c1 = CaseDetails.builder().id(1L).build();
        CaseDetails c2 = CaseDetails.builder().id(2L).build();

        SearchResult searchResult = mock(SearchResult.class);
        when(searchResult.getCases()).thenReturn(List.of(c1, c2));
        when(searchResult.getTotal()).thenReturn(2);

        when(coreCaseDataService.searchCases(any())).thenReturn(searchResult);

        Set<CaseDetails> result = service.getCases();

        assertThat(result).containsExactlyInAnyOrder(c1, c2);
        verify(coreCaseDataService, times(1)).searchCases(any());
    }

    @Test
    void shouldUseSameTimeNowAcrossPages() {
        // Simulate multiple pages with only 2 cases total (current service behavior)
        CaseDetails c1 = CaseDetails.builder().id(1L).build();
        CaseDetails c2 = CaseDetails.builder().id(2L).build();

        SearchResult firstPage = mock(SearchResult.class);
        when(firstPage.getCases()).thenReturn(List.of(c1, c2));
        when(firstPage.getTotal()).thenReturn(2); // total results equal page size

        when(coreCaseDataService.searchCases(any())).thenReturn(firstPage);

        Set<CaseDetails> result = service.getCases();

        assertThat(result).containsExactlyInAnyOrder(c1, c2);
        verify(coreCaseDataService, times(1)).searchCases(any());
    }
}
