package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class HearingFeeDueSearchServiceTest extends ElasticSearchServiceTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        searchService = new HearingFeeDueSearchService(coreCaseDataService, featureToggleService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        if (featureToggleService.isMintiEnabled()) {
            BoolQueryBuilder query = boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(boolQuery().must(matchQuery("state", "HEARING_READINESS"))));
            return new Query(query, List.of("reference"), fromValue);
        } else {
            BoolQueryBuilder query = boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.hearingDueDate").lt(LocalDate.now()
                                                                           .atTime(LocalTime.MIN)
                                                                           .toString()))
                            .must(boolQuery().must(matchQuery("state", "HEARING_READINESS"))));
            return new Query(query, List.of("reference"), fromValue);
        }
    }

    @Test
    void testQuery() {
        when(featureToggleService.isMintiEnabled()).thenReturn(true);
        featureToggleEnabled();

        when(featureToggleService.isMintiEnabled()).thenReturn(false);
        featureToggleNotEnabled();
    }

    private void featureToggleEnabled() {
        // Test case when feature toggle is enabled
        Query expectedQuery = buildQuery(0);
        String queryString = expectedQuery.toString();
        assertFalse(queryString.contains("data.hearingDueDate"), "Query should not contain 'data.hearingDueDate' when the feature toggle is enabled");
    }

    private void featureToggleNotEnabled() {
        // Test case when feature toggle is disabled
        Query expectedQuery = buildQuery(0);
        String queryString = expectedQuery.toString();
        assertTrue(queryString.contains("data.hearingDueDate"), "Query should contain 'data.hearingDueDate' when the feature toggle is disabled");
    }

    @Override
    protected Query buildQueryInMediation(int fromValue, LocalDate date, boolean carmEnabled,
                                          boolean initialSearch,
                                          String searchAfterValue) {
        return null;
    }
}
