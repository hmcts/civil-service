package uk.gov.hmcts.reform.civil.ga.service.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STAY_THE_CLAIM;

@ExtendWith(SpringExtension.class)
abstract class GaElasticSearchServiceTest {

    @Captor
    protected ArgumentCaptor<Query> queryCaptor;

    @Mock
    protected GaCoreCaseDataService coreCaseDataService;

    protected GaElasticSearchService searchService;

    @Test
    void shouldCallGetCasesOnce_WhenCasesReturnEqualsTotalCases() {
        SearchResult searchResult = buildSearchResultWithTotalCases(1);

        when(coreCaseDataService.searchGeneralApplication(any())).thenReturn(searchResult);

        searchService.getGeneralApplications(CaseState.AWAITING_WRITTEN_REPRESENTATIONS);

        verify(coreCaseDataService).searchGeneralApplication(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).usingRecursiveComparison()
            .isEqualTo(buildQuery(0, CaseState.AWAITING_WRITTEN_REPRESENTATIONS));
    }

    @Test
    void shouldCallGetCasesOnce_WhenNoCasesReturned() {
        SearchResult searchResult = buildSearchResult(0, emptyList());

        when(coreCaseDataService.searchGeneralApplication(any())).thenReturn(searchResult);

        assertThat(searchService.getOrderMadeGeneralApplications(CaseState.ORDER_MADE, STAY_THE_CLAIM)).isEmpty();
        verify(coreCaseDataService).searchGeneralApplication(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).usingRecursiveComparison()
            .isEqualTo(queryForOrderMade(0, CaseState.ORDER_MADE, STAY_THE_CLAIM));
    }

    @Test
    void shouldCallGetCasesOnce_WhenCasesRetrievedEqualsEsSearchLimit() {
        SearchResult searchResult = buildSearchResultWithTotalCases(10);

        when(coreCaseDataService.searchGeneralApplication(any())).thenReturn(searchResult);

        assertThat(searchService.getGeneralApplications(CaseState.AWAITING_RESPONDENT_RESPONSE)).hasSize(1);
        verify(coreCaseDataService).searchGeneralApplication(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).usingRecursiveComparison()
            .isEqualTo(buildQuery(0, CaseState.AWAITING_RESPONDENT_RESPONSE));
    }

    @Test
    void shouldCallGetCasesMultipleTimes_WhenCasesReturnedIsMoreThanEsSearchLimit() {
        SearchResult searchResult = buildSearchResultWithTotalCases(11);

        when(coreCaseDataService.searchGeneralApplication(any())).thenReturn(searchResult);

        searchService.getOrderMadeGeneralApplications(CaseState.ORDER_MADE, STAY_THE_CLAIM);
        verify(coreCaseDataService, times(2)).searchGeneralApplication(queryCaptor.capture());

        List<Query> capturedQueries = queryCaptor.getAllValues();
        assertThat(capturedQueries.get(0)).usingRecursiveComparison()
            .isEqualTo(queryForOrderMade(0, CaseState.ORDER_MADE, STAY_THE_CLAIM));
        assertThat(capturedQueries.get(1)).usingRecursiveComparison()
            .isEqualTo(queryForOrderMade(10, CaseState.ORDER_MADE, STAY_THE_CLAIM));
    }

    @Test
    void shouldCallGetCasesOnce_WhenNoCasesReturned_ForBusinessProcess() {
        SearchResult searchResult = buildSearchResult(0, emptyList());

        when(coreCaseDataService.searchGeneralApplication(any())).thenReturn(searchResult);

        assertThat(searchService.getGeneralApplicationsWithBusinessProcess(BusinessProcessStatus.STARTED)).isEmpty();
        verify(coreCaseDataService).searchGeneralApplication(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).usingRecursiveComparison()
            .isEqualTo(queryForBusinessProcessStatus(0, BusinessProcessStatus.STARTED));
    }

    @Test
    void shouldCallGetCasesMultipleTimes_WhenCasesReturnedIsMoreThanEsSearchLimit_ForBusinessProcess() {
        SearchResult searchResult = buildSearchResultWithTotalCases(11);

        when(coreCaseDataService.searchGeneralApplication(any())).thenReturn(searchResult);

        searchService.getGeneralApplicationsWithBusinessProcess(BusinessProcessStatus.STARTED);

        verify(coreCaseDataService, times(2)).searchGeneralApplication(queryCaptor.capture());

        List<Query> capturedQueries = queryCaptor.getAllValues();
        assertThat(capturedQueries.get(0)).usingRecursiveComparison()
            .isEqualTo(queryForBusinessProcessStatus(0, BusinessProcessStatus.STARTED));
        assertThat(capturedQueries.get(1)).usingRecursiveComparison()
            .isEqualTo(queryForBusinessProcessStatus(10, BusinessProcessStatus.STARTED));
    }

    protected SearchResult buildSearchResultWithTotalCases(int i) {
        return buildSearchResult(i, List.of(CaseDetails.builder().id(1L).build()));
    }

    protected SearchResult buildSearchResult(int i, List<CaseDetails> caseDetails) {
        return SearchResult.builder()
            .total(i)
            .cases(caseDetails)
            .build();
    }

    protected abstract Query buildQuery(int fromValue, CaseState caseState);

    protected abstract Query queryForOrderMade(int fromValue, CaseState caseState, GeneralApplicationTypes gaType);

    protected abstract Query queryForBusinessProcessStatus(int startIndex, BusinessProcessStatus processStatus);
}
