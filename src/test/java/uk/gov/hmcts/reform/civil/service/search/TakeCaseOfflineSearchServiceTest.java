package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TakeCaseOfflineSearchServiceTest extends ElasticSearchServiceTest {

    private static final String TEST_TIMESTAMP = "2024-01-01T00:00:00Z";
    private String capturedTimeNow;
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        featureToggleService = mock(FeatureToggleService.class);
        searchService = new TakeCaseOfflineSearchService(coreCaseDataService, featureToggleService) {
            @Override
            public Query query(int startIndex, String timeNow) {
                capturedTimeNow = timeNow;
                return super.query(startIndex, timeNow);
            }
        };
    }

    @Override
    protected Query buildQuery(int fromValue) {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        String timeNow = capturedTimeNow != null ? capturedTimeNow : "now";
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.applicant1ResponseDeadline").lt(timeNow))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_APPLICANT_INTENTION")))
                        .mustNot(matchQuery("data.isMintiLipCase", "Yes"))
                        .must(((TakeCaseOfflineSearchService) searchService).haveNoOngoingBusinessProcess()))
            .should(boolQuery()
                        .must(rangeQuery("data.addLegalRepDeadlineRes1").lt(timeNow))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT")))
                        .must(((TakeCaseOfflineSearchService) searchService).haveNoOngoingBusinessProcess()))
            .should(boolQuery()
                        .must(rangeQuery("data.addLegalRepDeadlineRes2").lt(timeNow))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT")))
                        .must(((TakeCaseOfflineSearchService) searchService).haveNoOngoingBusinessProcess()));

        return new Query(query, List.of("reference"), fromValue);
    }

    private Query buildWelshEnabledQuery(int fromValue) {
        String timeNow = capturedTimeNow != null ? capturedTimeNow : "now";
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.applicant1ResponseDeadline").lt(timeNow))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_APPLICANT_INTENTION")))
                        .mustNot(matchQuery("data.isMintiLipCase", "Yes"))
                        .mustNot(existsQuery("data.applicant1ResponseDate"))
                        .must(((TakeCaseOfflineSearchService) searchService).haveNoOngoingBusinessProcess()))
            .should(boolQuery()
                        .must(rangeQuery("data.addLegalRepDeadlineRes1").lt(timeNow))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT")))
                        .must(((TakeCaseOfflineSearchService) searchService).haveNoOngoingBusinessProcess()))
            .should(boolQuery()
                        .must(rangeQuery("data.addLegalRepDeadlineRes2").lt(timeNow))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT")))
                        .must(((TakeCaseOfflineSearchService) searchService).haveNoOngoingBusinessProcess()));

        return new Query(query, List.of("reference"), fromValue);
    }

    @Test
    void shouldReturnQuery_whenWelshFeatureDisabled() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

        Query query = searchService.query(0, TEST_TIMESTAMP);

        assertThat(query).isNotNull();
        assertThat(query.toString()).contains("\"from\": 0");
        assertThat(query.toString()).contains("\"_source\": [\"reference\"]");
        assertThat(query.toString()).contains("\"query\"");
    }

    @Test
    void shouldReturnQuery_whenWelshFeatureEnabled() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

        Query query = searchService.query(0, TEST_TIMESTAMP);

        assertThat(query).isNotNull();
        assertThat(query.toString()).contains("\"from\": 0");
        assertThat(query.toString()).contains("\"_source\": [\"reference\"]");
        assertThat(query.toString()).contains("\"query\"");
        assertThat(query).usingRecursiveComparison().isEqualTo(buildWelshEnabledQuery(0));
    }

    @Test
    void haveNoOngoingBusinessProcess_shouldBuildQueryWithStatusFinished() {
        BoolQueryBuilder query = ((TakeCaseOfflineSearchService) searchService).haveNoOngoingBusinessProcess();

        assertThat(query.toString()).contains("data.businessProcess.status");
        assertThat(query.toString()).contains("FINISHED");
    }
}
