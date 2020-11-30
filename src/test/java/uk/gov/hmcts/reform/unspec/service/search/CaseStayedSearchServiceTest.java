package uk.gov.hmcts.reform.unspec.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.unspec.model.search.Query;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class CaseStayedSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new CaseStayedSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .must(rangeQuery("last_modified").lt("now-6M"))
            .must(boolQuery()
                        .minimumShouldMatch(1)
                        .should(matchQuery("state", "CREATED"))
                        .should(matchQuery("state", "AWAITING_RESPONDENT_ACTION")));

        return new Query(query, List.of("reference"), fromValue);
    }
}
