package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class CaseDismissedSearchServiceTest extends ElasticSearchServiceTest {

    private String capturedTimeNow;

    @BeforeEach
    void setup() {
        searchService = new CaseDismissedSearchService(coreCaseDataService) {
            @Override
            public Query query(int startIndex, String timeNow) {
                capturedTimeNow = timeNow;
                return super.query(startIndex, timeNow);
            }
        };
    }

    @Override
    protected Query buildQuery(int fromValue) {
        String timeNow = capturedTimeNow != null ? capturedTimeNow : "now";
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.claimDetailsNotificationDeadline").lt(timeNow))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_CASE_DETAILS_NOTIFICATION"))))
            .should(boolQuery()
                        .must(rangeQuery("data.claimNotificationDeadline").lt(timeNow))
                        .must(boolQuery().must(matchQuery("state", "CASE_ISSUED"))))
            .should(boolQuery()
                        .must(rangeQuery("data.claimDismissedDeadline").lt(timeNow))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT"))));

        return new Query(query, List.of("reference"), fromValue);
    }
}
