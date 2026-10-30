package uk.gov.hmcts.reform.civil.service.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

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
        Query query = service.query(0);
        String queryStr = query.toString().replaceAll("\\s+", ""); // remove all spaces

        assertThat(queryStr)
            .contains(
                "\"from\":0",
                "\"_source\":[\"reference\"]",
                "finished"
            )
            .doesNotContain("last_modified", "range", "now-")
            .contains("must_not", "data.businessProcess.status.keyword", "case_insensitive",
                      "\"sort\":[{\"reference.keyword\":\"asc\"}]");
    }

    @Test
    void shouldReturnAllResultsFromGetCases() {
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
    void shouldReturnAllCasesAcrossMultiplePages() {
        List<CaseDetails> cases = IntStream.rangeClosed(1, 23)
            .mapToObj(id -> CaseDetails.builder().id((long) id).build()).toList();
        when(coreCaseDataService.searchCases(any())).thenReturn(
            SearchResult.builder().total(23).cases(cases.subList(0, 10)).build(),
            SearchResult.builder().total(23).cases(cases.subList(10, 20)).build(),
            SearchResult.builder().total(23).cases(cases.subList(20, 23)).build()
        );

        assertThat(service.getCases()).containsExactlyInAnyOrderElementsOf(cases);

        ArgumentCaptor<Query> queries = ArgumentCaptor.forClass(Query.class);
        verify(coreCaseDataService, times(3)).searchCases(queries.capture());
        assertThat(queries.getAllValues()).extracting(Query::toString).satisfiesExactly(
            query -> assertThat(query).contains("\"from\": 0").doesNotContain("last_modified"),
            query -> assertThat(query).contains("\"from\": 10").doesNotContain("last_modified"),
            query -> assertThat(query).contains("\"from\": 20").doesNotContain("last_modified")
        );
    }

    @Test
    void shouldReturnEmptySetWhenNoCasesMatch() {
        when(coreCaseDataService.searchCases(any()))
            .thenReturn(SearchResult.builder().total(0).cases(List.of()).build());

        assertThat(service.getCases()).isEmpty();
        verify(coreCaseDataService).searchCases(any());
    }
}
