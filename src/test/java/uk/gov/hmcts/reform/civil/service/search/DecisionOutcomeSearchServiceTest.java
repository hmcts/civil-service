package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class DecisionOutcomeSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new DecisionOutcomeSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                .must(rangeQuery("data.hearingDate").lte("now"))
                .must(boolQuery().must(matchQuery("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING"))));

        return new Query(query, List.of("reference"), fromValue);
    }
}
