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
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

        Query query = searchService.query(0, "now");

        assertThat(query).isNotNull();
        assertThat(query.toString()).contains("\"from\": 0");
        assertThat(query.toString()).contains("\"_source\": [\"reference\"]");
        assertThat(query.toString()).contains("\"query\"");
    }

    @Test
    void shouldReturnQuery_whenWelshFeatureEnabled() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

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
}
