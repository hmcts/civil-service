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

    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        featureToggleService = mock(FeatureToggleService.class);
        searchService = new TakeCaseOfflineSearchService(coreCaseDataService, featureToggleService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.applicant1ResponseDeadline").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_APPLICANT_INTENTION")))
                        .mustNot(matchQuery("data.isMintiLipCase", "Yes"))
                        .mustNot(existsQuery("data.applicant1ResponseDate")))
            .should(boolQuery()
                        .must(rangeQuery("data.addLegalRepDeadlineRes1").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT"))))
            .should(boolQuery()
                        .must(rangeQuery("data.addLegalRepDeadlineRes2").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT"))));

        return new Query(query, List.of("reference"), fromValue);
    }

    @Test
    void shouldReturnQuery_whenWelshFeatureEnabled() {

        Query query = searchService.query(0);

        assertThat(query).isNotNull();
        assertThat(query.toString()).contains("\"from\": 0");
        assertThat(query.toString()).contains("\"_source\": [\"reference\"]");
        assertThat(query.toString()).contains("\"query\"");
    }
}
