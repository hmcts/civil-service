package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefendantResponseDeadlineCheckSearchServiceTest {

    private static final String MOCK_TIMESTAMP = "2025-07-08T10:00:00Z";

    private CoreCaseDataService coreCaseDataService;
    private FeatureToggleService featureToggleService;
    private DefendantResponseDeadlineCheckSearchService searchService;

    @BeforeEach
    void setUp() {
        coreCaseDataService = mock(CoreCaseDataService.class);
        featureToggleService = mock(FeatureToggleService.class);
        searchService = new DefendantResponseDeadlineCheckSearchService(coreCaseDataService, featureToggleService);
    }

    @Test
    void shouldReturnQuery_whenWelshFeatureDisabled() {

        Query query = searchService.query(0, "now");

        assertThat(query).isNotNull();
        assertThat(query.toString()).contains("\"from\": 0");
        assertThat(query.toString()).contains("\"_source\": [\"reference\"]");
        assertThat(query.toString()).contains("\"query\"");
    }

    @Test
    void shouldReturnQuery_whenWelshFeatureEnabled() {

        Query query = searchService.query(0, MOCK_TIMESTAMP);

        assertThat(query).isNotNull();
        assertThat(query.toString()).contains("\"from\": 0");
        assertThat(query.toString()).contains("\"_source\": [\"reference\"]");
        assertThat(query.toString()).contains("\"query\"");
    }

    @Test
    void shouldReturnCasesFromAllPages() {
        // Given
        CaseDetails case1 = CaseDetails.builder().id(1L).build();
        CaseDetails case2 = CaseDetails.builder().id(2L).build();

        SearchResult allPages = SearchResult.builder()
            .total(2)
            .cases(List.of(case1, case2))
            .build();

        when(coreCaseDataService.searchCases(any(Query.class)))
            .thenReturn(allPages);

        // When
        Set<CaseDetails> results = searchService.getCases();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(CaseDetails::getId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void beState_shouldBuildQueryWithCorrectState() {
        BoolQueryBuilder query = searchService.beState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
        assertThat(query.toString()).contains("state");
        assertThat(query.toString()).contains("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");
    }

    @Test
    void haveNoOngoingBusinessProcess_shouldBuildQueryWithStatusFinished() {
        BoolQueryBuilder query = searchService.haveNoOngoingBusinessProcess();
        assertThat(query.toString()).contains("data.businessProcess.status");
        assertThat(query.toString()).contains("FINISHED");
    }

    @Test
    void shouldReturnQuery_whenCallingQueryWithSingleParameter() {

        Query query = searchService.query(0);

        assertThat(query).isNotNull();
        assertThat(query.toString()).contains("\"from\": 0");
        assertThat(query.toString()).contains("\"_source\": [\"reference\"]");
        assertThat(query.toString()).contains("\"query\"");
    }

    @Test
    void shouldReturnCasesFromMultiplePages_whenTotalExceedsDefaultLimit() {
        List<CaseDetails> firstPageCases = List.of(
            CaseDetails.builder().id(1L).build(),
            CaseDetails.builder().id(2L).build(),
            CaseDetails.builder().id(3L).build(),
            CaseDetails.builder().id(4L).build(),
            CaseDetails.builder().id(5L).build(),
            CaseDetails.builder().id(6L).build(),
            CaseDetails.builder().id(7L).build(),
            CaseDetails.builder().id(8L).build(),
            CaseDetails.builder().id(9L).build(),
            CaseDetails.builder().id(10L).build()
        );

        List<CaseDetails> secondPageCases = List.of(
            CaseDetails.builder().id(11L).build(),
            CaseDetails.builder().id(12L).build(),
            CaseDetails.builder().id(13L).build(),
            CaseDetails.builder().id(14L).build(),
            CaseDetails.builder().id(15L).build()
        );

        SearchResult firstPage = SearchResult.builder()
            .total(15)
            .cases(firstPageCases)
            .build();

        SearchResult secondPage = SearchResult.builder()
            .total(15)
            .cases(secondPageCases)
            .build();

        when(coreCaseDataService.searchCases(any(Query.class)))
            .thenReturn(firstPage)  // First page
            .thenReturn(secondPage); // Second page

        Set<CaseDetails> results = searchService.getCases();

        assertThat(results).hasSize(15);
        assertThat(results).extracting(CaseDetails::getId)
            .containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L);
    }
}
