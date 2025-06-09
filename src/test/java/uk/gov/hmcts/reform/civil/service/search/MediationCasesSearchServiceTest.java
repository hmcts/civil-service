package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class MediationCasesSearchServiceTest {

    private static final LocalDateTime CARM_DATE = LocalDateTime.of(2024, 11, 5,
                                                                    7, 28, 35
    );

    @Captor
    private ArgumentCaptor<Query> queryCaptor;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private MediationCasesSearchService searchService;

    protected Query buildQuery(int fromValue) {
        return null;
    }

    @Test
    void shouldCallGetMediationCasesOnce_WhenCasesReturnEqualsTotalCases() {
        SearchResult searchResult = buildSearchResultWithTotalCases(1);

        when(coreCaseDataService.searchCases(any())).thenReturn(searchResult);

        assertThat(searchService.getInMediationCases(false)).isEqualTo(searchResult.getCases());
        verify(coreCaseDataService).searchCases(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).usingRecursiveComparison()
            .isEqualTo(buildQueryInMediation(0, false, false, null));
    }

    @Test
    void shouldCallGetMediationCasesOnce_WhenCasesReturnEqualsTotalCasesCarmEnabled() {
        SearchResult searchResult = buildSearchResultWithTotalCases(1);

        when(coreCaseDataService.searchMediationCases(any())).thenReturn(searchResult);

        assertThat(searchService.getInMediationCases(true)).isEqualTo(searchResult.getCases());
        verify(coreCaseDataService).searchMediationCases(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).usingRecursiveComparison()
            .isEqualTo(buildQueryInMediation(0, true, true, null));
    }

    @Test
    void shouldCallGetInMediationCasesOnce_WhenCasesRetrievedEqualsEsSearchLimit() {
        SearchResult searchResult = buildSearchResultWithTotalCases(10);

        when(coreCaseDataService.searchCases(any())).thenReturn(searchResult);

        assertThat(searchService.getInMediationCases(false)).hasSize(1);
        verify(coreCaseDataService).searchCases(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).usingRecursiveComparison()
            .isEqualTo(buildQueryInMediation(0, false, false, null));
    }

    @Test
    void shouldCallGetInMediationCasesOnce_WhenCasesRetrievedEqualsEsSearchLimitCarmEnabled() {
        SearchResult searchResult = buildSearchResultWithTotalCases(10);

        when(coreCaseDataService.searchMediationCases(any())).thenReturn(searchResult);

        assertThat(searchService.getInMediationCases(true)).hasSize(1);
        verify(coreCaseDataService).searchMediationCases(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).usingRecursiveComparison()
            .isEqualTo(buildQueryInMediation(0, true, true, null));
    }

    @Test
    void shouldCallGetInMediationCasesMultipleTimes_WhenCasesReturnedIsMoreThanEsSearchLimit() {
        SearchResult searchResult = buildSearchResultWithTotalCases(11);

        when(coreCaseDataService.searchCases(any())).thenReturn(searchResult);

        assertThat(searchService.getInMediationCases(false)).hasSize(2);
        verify(coreCaseDataService, times(2)).searchCases(queryCaptor.capture());

        List<Query> capturedQueries = queryCaptor.getAllValues();
        assertThat(capturedQueries.get(0)).usingRecursiveComparison()
            .isEqualTo(buildQueryInMediation(0, false, false, null));
        assertThat(capturedQueries.get(1)).usingRecursiveComparison()
            .isEqualTo(buildQueryInMediation(10, false, false, null));
    }

    @Test
    void shouldCallGetInMediationCasesMultipleTimes_WhenCasesReturnedIsMoreThanEsSearchLimitCarmEnabled() {
        SearchResult searchResult = buildSearchResultWithTotalCases(11);

        when(coreCaseDataService.searchMediationCases(any())).thenReturn(searchResult);

        assertThat(searchService.getInMediationCases(true)).hasSize(2);
        verify(coreCaseDataService, times(2)).searchMediationCases(queryCaptor.capture());

        List<Query> capturedQueries = queryCaptor.getAllValues();
        assertThat(capturedQueries.get(0)).usingRecursiveComparison()
            .isEqualTo(buildQueryInMediation(0, true, true, null));
        assertThat(capturedQueries.get(1)).usingRecursiveComparison()
            .isEqualTo(buildQueryInMediation(0, true, false, "1"));
    }

    protected Query buildQueryInMediation(int fromValue, boolean carmEnabled,
                                          boolean initialSearch,
                                          String searchAfterValue) {

        if (carmEnabled) {
            BoolQueryBuilder query = boolQuery()
                .must(matchAllQuery())
                .must(boolQuery().must(matchQuery("state", "IN_MEDIATION")))
                .must(boolQuery().must(rangeQuery("data.submittedDate").gte(CARM_DATE)))
                .must(rangeQuery("data.claimMovedToMediationOn")
                          .gte("now-8d/d").lt("now-1d/d"))
                .mustNot(matchQuery("data.mediationFileSentToMmt", "Yes"));
            return new Query(query, Collections.emptyList(), fromValue, initialSearch, searchAfterValue);
        } else {
            BoolQueryBuilder query = boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(boolQuery().must(matchQuery("state", "IN_MEDIATION")))
                            .must(boolQuery().must(rangeQuery("data.submittedDate").lt(CARM_DATE)))
                            .must(rangeQuery("data.claimMovedToMediationOn")
                                      .gte("now-8d/d").lt("now-1d/d")))
                .mustNot(matchQuery("data.mediationFileSentToMmt", "Yes"));

            return new Query(query, Collections.emptyList(), fromValue);
        }
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
}
