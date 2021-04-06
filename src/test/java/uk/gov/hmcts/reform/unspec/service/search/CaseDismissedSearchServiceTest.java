package uk.gov.hmcts.reform.unspec.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.unspec.model.search.Query;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class CaseDismissedSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new CaseDismissedSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.claimDetailsNotificationDeadline").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_CASE_DETAILS_NOTIFICATION"))))
            .should(boolQuery()
                        .must(rangeQuery("data.claimNotificationDeadline").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "CASE_ISSUED"))))
            .should(boolQuery()
                        .must(rangeQuery("data.claimDismissedDeadline").lt("now"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT"))));

        return new Query(query, List.of("reference"), fromValue);
    }
}
